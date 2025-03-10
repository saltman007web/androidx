/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.pdf.viewer.fragment

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout.GONE
import android.widget.LinearLayout.VISIBLE
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.pdf.view.PdfView
import androidx.pdf.view.search.PdfSearchView
import androidx.pdf.viewer.fragment.model.PdfFragmentUiState.DocumentError
import androidx.pdf.viewer.fragment.model.PdfFragmentUiState.DocumentLoaded
import androidx.pdf.viewer.fragment.model.PdfFragmentUiState.Loading
import androidx.pdf.viewer.fragment.model.PdfFragmentUiState.PasswordRequested
import androidx.pdf.viewer.fragment.search.PdfSearchViewManager
import kotlinx.coroutines.launch

@RestrictTo(RestrictTo.Scope.LIBRARY)
public open class PdfViewerFragmentV2 : Fragment() {

    /**
     * The URI of the PDF document to display defaulting to `null`.
     *
     * When this property is set, the fragment begins loading the PDF document. A visual indicator
     * is displayed while the document is being loaded. Once the loading is fully completed, the
     * [onLoadDocumentSuccess] callback is invoked. If an error occurs during the loading phase, the
     * [onLoadDocumentError] callback is invoked with the exception.
     *
     * <p>Note: This property is recommended to be set when the fragment is in the started state.
     */
    public var documentUri: Uri?
        get() = documentViewModel.documentUriFromState
        set(value) {
            documentViewModel.loadDocument(uri = value, password = null)
        }

    /**
     * Controls whether text search mode is active. Defaults to false.
     *
     * When text search mode is activated, the search menu becomes visible, and search functionality
     * is enabled. Deactivating text search mode hides the search menu, clears search results, and
     * removes any search-related highlights.
     *
     * <p>Note: This property can only be set after the document has successfully loaded
     * i.e.[onLoadDocumentSuccess] is triggered. Any attempts to change it beforehand will have no
     * effect.
     */
    public var isTextSearchActive: Boolean
        get() = documentViewModel.isTextSearchActiveFromState
        set(value) {
            documentViewModel.updateSearchState(value)
        }

    /**
     * Indicates whether the toolbox should be visible.
     *
     * The host app can control this property to show/hide the toolbox based on its state and the
     * `onRequestImmersiveMode` callback. The setter updates the UI elements within the fragment
     * accordingly.
     */
    public var isToolboxVisible: Boolean
        get() = documentViewModel.isToolboxVisibleFromState
        set(value) {
            documentViewModel.onToolboxViewToggle(value)
        }

    /**
     * Called when the PDF view wants to enter or exit immersive mode based on user's interaction
     * with the content. Apps would typically hide their top bar or other navigational interface
     * when in immersive mode. The default implementation keeps toolbox visibility in sync with the
     * enterImmersive mode. It is recommended that apps keep this behaviour by calling
     * super.onRequestImmersiveMode while overriding this method.
     *
     * @param enterImmersive true to enter immersive mode, false to exit.
     */
    @CallSuper
    public open fun onRequestImmersiveMode(enterImmersive: Boolean) {
        // Update toolbox visibility
        isToolboxVisible = !enterImmersive
    }

    /**
     * Invoked when the document has been fully loaded, processed, and the initial pages are
     * displayed within the viewing area. This callback signifies that the document is ready for
     * user interaction.
     *
     * <p>Note that this callback is dispatched only when the fragment is fully created and not yet
     * destroyed, i.e., after [onCreate] has fully run and before [onDestroy] runs, and only on the
     * main thread.
     */
    public open fun onLoadDocumentSuccess() {}

    /**
     * Invoked when a problem arises during the loading process of the PDF document. This callback
     * provides details about the encountered error, allowing for appropriate error handling and
     * user notification.
     *
     * <p>Note that this callback is dispatched only when the fragment is fully created and not yet
     * destroyed, i.e., after [onCreate] has fully run and before [onDestroy] runs, and only on the
     * main thread.
     *
     * @param error [Throwable] that occurred during document loading.
     */
    @Suppress("UNUSED_PARAMETER") public open fun onLoadDocumentError(error: Throwable) {}

    private val documentViewModel: PdfDocumentViewModel by viewModels {
        PdfDocumentViewModel.Factory
    }

    private lateinit var pdfView: PdfView
    private lateinit var errorView: TextView
    private lateinit var loadingView: ProgressBar
    private lateinit var pdfSearchView: PdfSearchView
    private lateinit var pdfSearchViewManager: PdfSearchViewManager

    // Provides visible pages in viewport both end inclusive.
    private val PdfView.visiblePages: IntRange
        get() = IntRange(firstVisiblePage, firstVisiblePage + visiblePagesCount - 1)

    private val searchQueryTextWatcher =
        object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No-Op.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                documentViewModel.searchDocument(
                    query = s.toString(),
                    visiblePageRange = pdfView.visiblePages
                )
            }

            override fun afterTextChanged(s: Editable?) {
                // No-Op.
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.pdf_viewer_fragment, container, false)
        pdfView = root.findViewById(R.id.pdfView)
        errorView = root.findViewById(R.id.errorTextView)
        loadingView = root.findViewById(R.id.pdfLoadingProgressBar)
        pdfSearchView = root.findViewById(R.id.pdfSearchView)
        pdfSearchViewManager = PdfSearchViewManager(pdfSearchView)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onPdfSearchViewCreated(pdfSearchView)

        collectFlowOnLifecycleScope { collectFragmentUiScreenState() }
    }

    /**
     * Called from Fragment.onViewCreated(). This gives subclasses a chance to customize component.
     */
    protected fun onPdfSearchViewCreated(pdfSearchView: PdfSearchView) {
        setupSearchViewListeners(pdfSearchView)
        collectSearchUiStates()
        // TODO(b/382307165): add animator to align with keyboard
    }

    private fun setupSearchViewListeners(pdfSearchView: PdfSearchView) {
        with(pdfSearchView) {
            searchQueryBox.addTextChangedListener(searchQueryTextWatcher)

            searchQueryBox.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch()
                }
                true // IME action consumed
            }
            findPrevButton.setOnClickListener {
                searchQueryBox.clearFocus()
                documentViewModel.findPreviousMatch()
            }
            findNextButton.setOnClickListener {
                searchQueryBox.clearFocus()
                documentViewModel.findNextMatch()
            }
            closeButton.setOnClickListener { isTextSearchActive = false }
        }
    }

    private fun PdfSearchView.performSearch() {
        searchQueryBox.clearFocus()

        searchDocument(searchQueryBox.text.toString())
    }

    private fun searchDocument(query: String) {
        documentViewModel.searchDocument(query = query, visiblePageRange = pdfView.visiblePages)
    }

    private fun collectSearchUiStates() {
        collectFlowOnLifecycleScope {
            documentViewModel.searchViewUiState.collect { uiState ->
                pdfSearchViewManager.setState(uiState)
            }
        }
    }

    /**
     * Collects the UI state of the fragment and updates the views accordingly.
     *
     * This is a suspend function that continuously observes the fragment's UI state and updates the
     * corresponding views to reflect the latest state. This ensures that the UI remains
     * synchronized with any changes in the underlying data or user interactions.
     */
    private suspend fun collectFragmentUiScreenState() {
        documentViewModel.fragmentUiScreenState.collect { uiState ->
            when (uiState) {
                is Loading -> handleLoading()
                is PasswordRequested -> handlePasswordRequested()
                is DocumentLoaded -> handleDocumentLoaded(uiState)
                is DocumentError -> handleDocumentError(uiState)
            }
        }
    }

    private fun handleLoading() {
        setViewVisibility(pdfView = GONE, loadingView = VISIBLE, errorView = GONE)
    }

    private fun handlePasswordRequested() {
        setViewVisibility(pdfView = GONE, loadingView = GONE, errorView = GONE)
        // Utilize retry param to show incorrect password on PasswordDialog
    }

    private fun handleDocumentLoaded(uiState: DocumentLoaded) {
        onLoadDocumentSuccess()
        pdfView.pdfDocument = uiState.pdfDocument
        setViewVisibility(pdfView = VISIBLE, loadingView = GONE, errorView = GONE)
    }

    private fun handleDocumentError(uiState: DocumentError) {
        onLoadDocumentError(uiState.exception)
        setViewVisibility(pdfView = GONE, loadingView = GONE, errorView = VISIBLE)
    }

    private fun setViewVisibility(pdfView: Int, loadingView: Int, errorView: Int) {
        this.pdfView.visibility = pdfView
        this.loadingView.visibility = loadingView
        this.errorView.visibility = errorView
    }

    private fun collectFlowOnLifecycleScope(block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            /**
             * [repeatOnLifecycle] launches the block in a new coroutine every time the lifecycle is
             * in the STARTED state (or above) and cancels it when it's STOPPED.
             */
            repeatOnLifecycle(Lifecycle.State.STARTED) { block() }
        }
    }
}
