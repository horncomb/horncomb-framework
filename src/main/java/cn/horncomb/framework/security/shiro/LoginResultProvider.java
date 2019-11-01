package cn.horncomb.framework.security.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface LoginResultProvider {

    <T> ResponseEntity<T> handleSuccessResult(AuthenticationToken token, Subject subject,
                                              HttpServletRequest request) throws IOException;

    <T> ResponseEntity<T> handleFailureResult(ResponseEntity<Problem> probleResponse, AuthenticationToken token,
                                              Subject subject, HttpServletRequest request) throws IOException;
}
