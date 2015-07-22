/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.httpclient.httprequest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.protocol.HTTP;

/**
 * Implementation for POST.
 * It is like <a href="https://svn.apache.org/repos/asf/httpcomponents/httpclient/tags/4.2.3/httpclient/src/main/java/org/apache/http/client/methods/HttpEntityEnclosingRequestBase.java">this</a>.
 * @author davidepastore
 */
public abstract class NUHttpEntityEnclosingRequestBase extends NUHttpRequestBase implements HttpEntityEnclosingRequest{
    
    
    private HttpEntity entity;

    public NUHttpEntityEnclosingRequestBase() {
        super();
    }

    @Override
    public HttpEntity getEntity() {
        return this.entity;
    }

    @Override
    public void setEntity(final HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean expectContinue() {
        Header expect = getFirstHeader(HTTP.EXPECT_DIRECTIVE);
        return expect != null && HTTP.EXPECT_CONTINUE.equalsIgnoreCase(expect.getValue());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        HttpEntityEnclosingRequestBase clone =
            (HttpEntityEnclosingRequestBase) super.clone();
        if (this.entity != null) {
            clone.setEntity(this.entity);
        }
        return clone;
    }
    
}
