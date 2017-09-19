package org.bonitasoft.console.common.server.login.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

@RunWith(MockitoJUnitRunner.class)
public class TokenGeneratorTest {

    private final HttpSession session = new MockHttpSession();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    private final static String CONTEXT_PATH = "bonitaTest";
    private final static String BONITA_TOKEN_NAME = "X-Bonita-API-Token";
    private final static String BONITA_TOKEN_VALUE = "sdfsdfjhvèzv";

    @Spy
    TokenGenerator tokenGenerator = new TokenGenerator();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void setUp() throws Exception {
        request.setContextPath(CONTEXT_PATH);
    }

    private Cookie getCookieByNameAndPath(MockHttpServletResponse response, String name, String path) {
        for (Cookie cookie : response.getCookies()) {
            if (cookie.getName().equals(name) && cookie.getPath().equals(path)) {
                return cookie;
            }
        }
        return null;
    }

    private Cookie aCookie(String name, String value, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        return cookie;
    }

    @Test
    public void should_create_token_and_store_it_in_session() throws Exception {
        final String token = tokenGenerator.createOrLoadToken(session);
        assertThat(token).isNotEmpty();
        assertThat(session.getAttribute(TokenGenerator.API_TOKEN)).isEqualTo(token);
    }

    @Test
    public void should_load_token_from_session() throws Exception {
        session.setAttribute(TokenGenerator.API_TOKEN, BONITA_TOKEN_VALUE);
        final String token = tokenGenerator.createOrLoadToken(session);
        assertThat(token).isNotEmpty();
        assertThat(token).isEqualTo(BONITA_TOKEN_VALUE);
    }

    @Test
    public void testSetTokenToResponseCookie() throws Exception {
        tokenGenerator.setTokenToResponseCookie(request, response, BONITA_TOKEN_VALUE);

        final Cookie csrfCookie = response.getCookie(BONITA_TOKEN_NAME);

        assertThat(csrfCookie.getName()).isEqualTo(BONITA_TOKEN_NAME);
        assertThat(csrfCookie.getPath()).isEqualTo(CONTEXT_PATH);
        assertThat(csrfCookie.getValue()).isEqualTo(BONITA_TOKEN_VALUE);
        assertThat(csrfCookie.getSecure()).isFalse();
    }

    @Test
    public void should_set_csrf_token_cookie_path_specified_via_system_property() throws Exception {
        System.setProperty("bonita.csrf.cookie.path", "/");
        request.setContextPath("somepath");

        tokenGenerator.setTokenToResponseCookie(request, response, "sdfsdfjhvèzv");

        Cookie csrfCookie = response.getCookie(BONITA_TOKEN_NAME);
        assertThat(csrfCookie.getPath()).isEqualTo("/");
    }
    
    @Test
    public void should_set_secure_csrf_token_cookie_when_specified() throws Exception {
        doReturn(true).when(tokenGenerator).isCSRFTokenCookieSecure();

        tokenGenerator.setTokenToResponseCookie(request, response, BONITA_TOKEN_VALUE);

        Cookie csrfCookie = response.getCookie(BONITA_TOKEN_NAME);
        assertThat(csrfCookie.getName()).isEqualTo(BONITA_TOKEN_NAME);
        assertThat(csrfCookie.getPath()).isEqualTo(CONTEXT_PATH);
        assertThat(csrfCookie.getValue()).isEqualTo(BONITA_TOKEN_VALUE);
        assertThat(csrfCookie.getSecure()).isTrue();
    }

    @Test
    public void testSetTokenToResponseHeader() throws Exception {
        tokenGenerator.setTokenToResponseHeader(response, BONITA_TOKEN_VALUE);

        assertThat(response.getHeader(BONITA_TOKEN_NAME)).isEqualTo(BONITA_TOKEN_VALUE);
    }

    @Test
    public void should_invalidate_csrf_cookie_already_existing_on_another_path_than_the_expected_one() throws Exception {
        System.setProperty("bonita.csrf.cookie.path", "/");
        request.setCookies(aCookie(BONITA_TOKEN_NAME, "aValue", "aPath"));

        tokenGenerator.setTokenToResponseCookie(request, response, "whatever");

        Cookie cookie = getCookieByNameAndPath(response, BONITA_TOKEN_NAME, "aPath");
        assertThat(cookie.getMaxAge()).isEqualTo(0);
        assertThat(cookie.getValue()).isEqualTo("");
    }
    
    @Test
    public void should_invalidate_csrf_cookie_already_existing_on_root_path() throws Exception {
        request.setCookies(aCookie(BONITA_TOKEN_NAME, "aValue", "aPath"));

        tokenGenerator.setTokenToResponseCookie(request, response, "whatever");

        Cookie cookie = getCookieByNameAndPath(response, BONITA_TOKEN_NAME, CONTEXT_PATH);
        assertThat(cookie.getValue()).isEqualTo("whatever");
        
        Cookie rootCookie = getCookieByNameAndPath(response, BONITA_TOKEN_NAME, "/");
        assertThat(rootCookie.getMaxAge()).isEqualTo(0);
        assertThat(rootCookie.getValue()).isEqualTo("");
    }
}
