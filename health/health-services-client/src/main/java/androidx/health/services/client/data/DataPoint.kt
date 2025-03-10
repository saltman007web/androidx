/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.health.services.client.data

import androidx.health.services.client.proto.DataProto
import androidx.health.services.client.proto.DataProto.AggregateDataPoint

/** Base class to represent individual pieces of data of type [dataType]. */
public abstract class DataPoint<T : Any>
internal constructor(
    /** Type of data contained within this [DataPoint]. */
    public open val dataType: DataType<T, out DataPoint<T>>,
) {
    internal companion object {
        internal fun fromProto(proto: AggregateDataPoint): DataPoint<*> {
            return if (proto.hasCumulativeDataPoint()) {
                CumulativeDataPoint.fromProto(proto.cumulativeDataPoint)
            } else {
                StatisticalDataPoint.fromProto(proto.statisticalDataPoint)
            }
        }

        internal fun fromProto(proto: DataProto.DataPoint): DataPoint<*> {
            return if (proto.dataType.timeType == DataProto.DataType.TimeType.TIME_TYPE_INTERVAL) {
                IntervalDataPoint.fromProto(proto)
            } else {
                SampleDataPoint.fromProto(proto)
            }
        }
    }
}
