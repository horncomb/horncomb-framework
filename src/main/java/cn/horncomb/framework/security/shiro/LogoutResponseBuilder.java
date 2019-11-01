package cn.horncomb.framework.security.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface LogoutResponseBuilder {
    void writeSuccessResponse(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
