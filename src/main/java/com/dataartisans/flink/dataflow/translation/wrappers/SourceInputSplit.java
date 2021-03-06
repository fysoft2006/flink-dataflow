/*
 * Copyright 2015 Data Artisans GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dataartisans.flink.dataflow.translation.wrappers;

import com.google.cloud.dataflow.sdk.io.Source;
import com.google.cloud.dataflow.sdk.util.SerializableUtils;
import org.apache.flink.core.io.InputSplit;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;

/**
 * {@link org.apache.flink.core.io.InputSplit} for
 * {@link com.dataartisans.flink.dataflow.translation.wrappers.SourceInputFormat}. We pass
 * the sharded Source around in the input split because Sources simply split up into several
 * Sources for sharding. This is different to how Flink creates a separate InputSplit from
 * an InputFormat.
 */
public class SourceInputSplit<T> implements InputSplit {

	private Source<T> source;
	private int splitNumber;

	public SourceInputSplit() {
	}

	public SourceInputSplit(Source<T> source, int splitNumber) {
		this.source = source;
		this.splitNumber = splitNumber;
	}

	@Override
	public int getSplitNumber() {
		return splitNumber;
	}

	public Source<T> getSource() {
		return source;
	}

	@Override
	public void write(DataOutputView out) throws IOException {
		out.writeInt(splitNumber);
		byte[] serializedSource = SerializableUtils.serializeToByteArray(source);
		out.writeInt(serializedSource.length);
		out.write(serializedSource);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void read(DataInputView in) throws IOException {
		splitNumber = in.readInt();
		int length = in.readInt();
		byte[] serializedSource = new byte[length];
		in.read(serializedSource, 0, length);
		source = (Source<T>) SerializableUtils.deserializeFromByteArray(serializedSource, "Input Source");
	}
}
