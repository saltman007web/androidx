/*
 * Copyright 2023 The Android Open Source Project
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

@file:JvmName("PagingDataTransforms")
@file:JvmMultifileClass

package androidx.paging

import androidx.annotation.CheckResult
import java.util.concurrent.Executor
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Returns a [PagingData] containing the result of applying the given [transform] to each element,
 * as it is loaded.
 *
 * @see PagingData.map
 */
@CheckResult
public fun <T : Any, R : Any> PagingData<T>.map(
    executor: Executor,
    transform: (T) -> R,
): PagingData<R> = transform { event ->
    withContext(executor.asCoroutineDispatcher()) { event.map { transform(it) } }
}

/**
 * Returns a [PagingData] of all elements returned from applying the given [transform] to each
 * element, as it is loaded.
 *
 * @see flatMap
 */
@CheckResult
public fun <T : Any, R : Any> PagingData<T>.flatMap(
    executor: Executor,
    transform: (T) -> Iterable<R>
): PagingData<R> = transform { event ->
    withContext(executor.asCoroutineDispatcher()) { event.flatMap { transform(it) } }
}

/**
 * Returns a [PagingData] containing only elements matching the given [predicate].
 *
 * @see filter
 */
@CheckResult
@JvmName("filter")
public fun <T : Any> PagingData<T>.filter(
    executor: Executor,
    predicate: (T) -> Boolean
): PagingData<T> = transform { event ->
    withContext(executor.asCoroutineDispatcher()) { event.filter { predicate(it) } }
}

// NOTE: samples in the doc below are manually imported from Java code in the samples
// project, since Java cannot be linked with @sample.
// DO NOT CHANGE THE BELOW COMMENT WITHOUT MAKING THE CORRESPONDING CHANGE IN `samples/`
/**
 * Returns a [PagingData] containing each original element, with an optional separator generated by
 * [generator], given the elements before and after (or null, in boundary conditions).
 *
 * Note that this transform is applied asynchronously, as pages are loaded. Potential separators
 * between pages are only computed once both pages are loaded.
 *
 * **Kotlin callers should instead use the suspending extension function variant of
 * insertSeparators**
 *
 * ```
 * /*
 *  * Create letter separators in an alphabetically sorted list.
 *  *
 *  * For example, if the input is:
 *  *     "apple", "apricot", "banana", "carrot"
 *  *
 *  * The operator would output:
 *  *     "A", "apple", "apricot", "B", "banana", "C", "carrot"
 *  */
 * pagingDataStream.map(pagingData ->
 *         // map outer stream, so we can perform transformations on each paging generation
 *         PagingDataTransforms.insertSeparators(pagingData, bgExecutor,
 *                 (@Nullable String before, @Nullable String after) -> {
 *                     if (after != null && (before == null
 *                             || before.charAt(0) != after.charAt(0))) {
 *                         // separator - after is first item that starts with its first
 *                         // letter
 *                         return Character.toString(
 *                                 Character.toUpperCase(after.charAt(0)));
 *                     } else {
 *                         // no separator - either end of list, or first
 *                         // letters of items are the same
 *                         return null;
 *                     }
 *                 }));
 *
 * /*
 *  * Create letter separators in an alphabetically sorted list of Items, with UiModel
 *  * objects.
 *  *
 *  * For example, if the input is (each an `Item`):
 *  *     "apple", "apricot", "banana", "carrot"
 *  *
 *  * The operator would output a list of UiModels corresponding to:
 *  *     "A", "apple", "apricot", "B", "banana", "C", "carrot"
 *  */
 * pagingDataStream.map(itemPagingData -> {
 *     // map outer stream, so we can perform transformations on each paging generation
 *
 *     // first convert items in stream to UiModel.Item
 *     PagingData<UiModel.ItemModel> itemModelPagingData = PagingDataTransforms.map(
 *             itemPagingData, bgExecutor, UiModel.ItemModel::new);
 *
 *     // Now insert UiModel.Separators, which makes the PagingData of generic type UiModel
 *     return PagingDataTransforms.insertSeparators(
 *             itemModelPagingData, bgExecutor,
 *             (@Nullable UiModel.ItemModel before, @Nullable UiModel.ItemModel after) -> {
 *                 if (after != null && (before == null
 *                         || before.item.label.charAt(0) != after.item.label.charAt(0))) {
 *                     // separator - after is first item that starts with its first letter
 *                     return new UiModel.SeparatorModel(
 *                             Character.toUpperCase(after.item.label.charAt(0)));
 *                 } else {
 *                     // no separator - either end of list, or first
 *                     // letters of items are the same
 *                     return null;
 *                 }
 *             });
 * });
 *
 * public class UiModel {
 *     static class ItemModel extends UiModel {
 *         public Item item;
 *         ItemModel(Item item) {
 *             this.item = item;
 *         }
 *     }
 *     static class SeparatorModel extends UiModel {
 *         public char character;
 *         SeparatorModel(char character) {
 *             this.character = character;
 *         }
 *     }
 * }
 * ```
 *
 * @param terminalSeparatorType [TerminalSeparatorType] used to configure when the header and footer
 *   are added.
 * @param executor [Executor] to run the [generator] function in.
 * @param generator Generator function used to construct a separator item given the item before and
 *   the item after. For terminal separators (header and footer), the arguments passed to the
 *   generator, `before` and `after`, will be `null` respectively. In cases where the fully
 *   paginated list is empty, a single separator will be added where both `before` and `after` items
 *   are `null`.
 */
@CheckResult
@JvmOverloads
public fun <R : Any, T : R> PagingData<T>.insertSeparators(
    terminalSeparatorType: TerminalSeparatorType = TerminalSeparatorType.FULLY_COMPLETE,
    executor: Executor,
    generator: (T?, T?) -> R?,
): PagingData<R> {
    return insertSeparators(terminalSeparatorType) { before, after ->
        withContext(executor.asCoroutineDispatcher()) { generator(before, after) }
    }
}
