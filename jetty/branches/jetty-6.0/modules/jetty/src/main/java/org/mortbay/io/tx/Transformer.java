package org.mortbay.io.tx;

import java.net.URL;

public interface Transformer
{
    byte[] transform(URL src, byte[] content);
}
