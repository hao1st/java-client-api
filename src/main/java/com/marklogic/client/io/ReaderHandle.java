/*
 * Copyright 2012 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import com.marklogic.client.Format;
import com.marklogic.client.io.marker.JSONReadHandle;
import com.marklogic.client.io.marker.JSONWriteHandle;
import com.marklogic.client.io.marker.StructureReadHandle;
import com.marklogic.client.io.marker.StructureWriteHandle;
import com.marklogic.client.io.marker.TextReadHandle;
import com.marklogic.client.io.marker.TextWriteHandle;
import com.marklogic.client.io.marker.XMLReadHandle;
import com.marklogic.client.io.marker.XMLWriteHandle;

/**
 * A Reader Handle represents a resource as a reader for reading or writing.
 * 
 * When finished with the reader, close the reader to release the resources.
 */
public class ReaderHandle
	extends BaseHandle<Reader, OutputStreamSender>
	implements OutputStreamSender,
		JSONReadHandle, JSONWriteHandle, 
		TextReadHandle, TextWriteHandle,
		XMLReadHandle, XMLWriteHandle,
		StructureReadHandle, StructureWriteHandle
{
	final static private int BUFFER_SIZE = 1024;

    private Reader content;

    public ReaderHandle() {
    }

    /**
	 * Returns a reader for a resource read from the database.
	 * 
     * When finished with the reader, close the reader to release
     * the response.
     * 
     * @return
     */
    public Reader get() {
    	return content;
    }
	public void set(Reader content) {
		this.content = content;
	}
	public ReaderHandle with(Reader content) {
		set(content);
		return this;
	}

	public ReaderHandle withFormat(Format format) {
		setFormat(format);
		return this;
	}
	public ReaderHandle withMimetype(String mimetype) {
		setMimetype(mimetype);
		return this;
	}

	@Override
	protected Class<Reader> receiveAs() {
		return Reader.class;
	}
	@Override
	protected void receiveContent(Reader content) {
		this.content = content;
	}
	@Override
	protected ReaderHandle sendContent() {
		if (content == null) {
			throw new IllegalStateException("No character stream to write");
		}

		return this;
	}

	@Override
	public void write(OutputStream out) throws IOException {
		Charset charset = Charset.forName("UTF-8");
		CharsetEncoder encoder = charset.newEncoder();

		CharBuffer charBuf = CharBuffer.allocate(BUFFER_SIZE);
		byte[] buf = new byte[BUFFER_SIZE * 2];
		ByteBuffer byteBuf = ByteBuffer.wrap(buf);

		while (content.read(charBuf) != -1) {
			encoder.reset();
			charBuf.flip();
			byteBuf.clear();
			CoderResult result = encoder.encode(charBuf, byteBuf, false);
			if (result.isError()) {
				throw new RuntimeException(result.toString());
			}
			byteBuf.flip();
			out.write(buf, 0, byteBuf.limit());
			charBuf.clear();
		}

		out.flush();
	}
}
