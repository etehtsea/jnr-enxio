/*
 * Copyright (C) 2008 Wayne Meissner
 *
 * This file is part of the JNR project.
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

package jnr.enxio.channels;

import jnr.constants.platform.Shutdown;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public class NativeSocketChannel extends AbstractSelectableChannel
        implements ByteChannel, NativeSelectableChannel {

    private final Common common;
    private final int validOps;

    public NativeSocketChannel(int fd) {
        this(NativeSelectorProvider.getInstance(), fd, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public NativeSocketChannel(int fd, int ops) {
        this(NativeSelectorProvider.getInstance(), fd, ops);
    }

    NativeSocketChannel(SelectorProvider provider, int fd, int ops) {
        super(provider);
        common = new Common(fd);
        this.validOps = ops;
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        Native.close(common.getFD());
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        Native.setBlocking(common.getFD(), block);
    }

    @Override
    public final int validOps() {
        return validOps;
    }
    public final int getFD() {
        return common.getFD();
    }

    public int read(ByteBuffer dst) throws IOException {
        return common.read(dst);
    }

    public int write(ByteBuffer src) throws IOException {
        return common.write(src);
    }
    
    public void shutdownInput() throws IOException {
        int n = Native.shutdown(common.getFD(), SHUT_RD);
        if (n < 0) {
            throw new IOException(Native.getLastErrorString());
        }
    }
    
    public void shutdownOutput() throws IOException {
        int n = Native.shutdown(common.getFD(), SHUT_WR);
        if (n < 0) {
            throw new IOException(Native.getLastErrorString());
        }
    }
    
    private final static int SHUT_RD = Shutdown.SHUT_RD.intValue();
    private final static int SHUT_WR = Shutdown.SHUT_WR.intValue();
}
