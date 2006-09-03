package org.mortbay.jetty.ajp;

import org.mortbay.io.Buffer;
import org.mortbay.io.View;

/**
 * @author Markus Kobler
 */
public class Ajp13RequestPacket {

    private Buffer _buffer;
    private View _tok0 = new View();
    private View _tok1 = new View();
    private byte _ch;
    private int _int;
    private boolean _parsedHiByte = false;
    private Buffer _str;
    private int _strStart = -1;
    private int _headerCount = -1;
    private Buffer _headerName;
    private byte _attrType;
    private String _attrKey;

    public void setBuffer(Buffer buffer) {
        _buffer = buffer;
        reset();
    }

    public void next() {
        _ch = _buffer.get();
    }

    public byte getByte() {
        return _ch;
    }

    public boolean getBool() {
        return _ch > 0;
    }

    public int getInt() {
        return _int;
    }

    public Buffer getString() {
        return _str;
    }

    public byte getAttributeType() {
        return _attrType;
    }

    public String getAttributeKey() {
        return _attrKey;
    }

    public Buffer getMethod() {
        return Ajp13PacketMethods.CACHE.get(_ch);
    }

    public Buffer getHeaderName() {
        return _headerName;
    }

    public boolean parsedInt() {
        if (_parsedHiByte = !_parsedHiByte) {
            _int = (_ch & 0xFF) << 8;
            return false;
        } else {
            _int += _ch & 0xFF;
            return true;
        }
    }

    public boolean parsedString() {
        if (_strStart < 0) {
            if (parsedInt()) {
                if (_int == 0xFFFF || _int < 0) {
                    _str = null;
                    return true;
                }
                _strStart = _buffer.getIndex();
            }
        } else {
            if (_ch == 0) {
                _tok0.update(_strStart, _buffer.getIndex() - 1);
                _str = _tok0;
                _strStart = -1;
                return true;
            }
        }
        return false;
    }

    public boolean parsedHeaderName() {
        if (_strStart < 0) {
            if (parsedInt()) {
                if ((0xFF00 & _int) == 0xA000) {
                    _headerName = Ajp13RequestHeaders.CACHE.get(_ch);
                    return true;
                } else {
                    _strStart = _buffer.getIndex();
                }
            }
        } else if (_ch == 0) {
            _tok1.update(_strStart, _buffer.getIndex() - 1);
            _headerName = _tok1;
            _strStart = -1;
            return true;
        }
        return false;
    }

    public boolean parsedHeaderCount() {
        if (parsedInt()) {
            _headerCount = _int;
            return true;
        }
        return false;
    }

    public boolean parsedHeaders() {
        return --_headerCount < 0;
    }

    public byte parsedAttributeType() {
        return _attrType = _ch;
    }

    public void setAttributeKey() {
        _attrKey = _str.toString();
    }


    public void reset() {
        _tok0.update(_buffer);
        _tok0.update(0, 0);
        _tok1.update(_buffer);
        _tok1.update(0, 0);
        _ch = 0;
        _parsedHiByte = false;
        _strStart = -1;
        _str = null;
        _headerCount = -1;
        _headerName = null;
    }

}
