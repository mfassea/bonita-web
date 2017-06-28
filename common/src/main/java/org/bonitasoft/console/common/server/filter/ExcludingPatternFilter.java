package org.bonitasoft.console.common.server.filter;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;


/**
 * @author Anthony Birembaut
 */
public abstract class ExcludingPatternFilter implements Filter {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(ExcludingPatternFilter.class.getName());
    
    /** the Pattern of url not to filter */
    protected Pattern excludePattern = null;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        initExcludePattern(filterConfig);
    }
    
    @Override
    public void destroy() {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String url = ((HttpServletRequest) request).getRequestURL().toString();
        if (matchExcludePatterns(url)) {
            excludePatternFiltering(request, response, chain);
        } else {
            proceedWithFiltering(request, response, chain);
        }
    }

    public void excludePatternFiltering(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    public abstract void proceedWithFiltering(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException;

    protected void initExcludePattern(final FilterConfig filterConfig) {
        final String contextPath = filterConfig.getServletContext().getContextPath();
        String webappName;
        if (contextPath.length() > 0) {
            webappName = contextPath.substring(1);
        } else {
            webappName = "";
        }
        excludePattern = compilePattern(StringUtils.isBlank(filterConfig.getInitParameter("excludePattern")) ? getDefaultExcludedPages()
                .replace("bonita", webappName)
                : filterConfig.getInitParameter("excludePattern"));
    }

    public abstract String getDefaultExcludedPages();

    public Pattern compilePattern(final String stringPattern) {
        if (StringUtils.isNotBlank(stringPattern)) {
            try {
                return Pattern.compile(stringPattern);
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, " impossible to create pattern from [" + stringPattern + "] : " + e);
            }
        }
        return null;
    }

    /**
     * check the given url against the local url exclude pattern
     *
     * @param url
     *        the url to check
     * @return true if the url match the pattern
     */
    public boolean matchExcludePatterns(final String url) {
        try {
            final boolean isExcluded = getExcludePattern() != null && getExcludePattern().matcher(new URL(url).getPath()).find();
            if (LOGGER.isLoggable(Level.FINE)) {
                if (isExcluded) {
                    LOGGER.log(Level.FINE, " Exclude pattern match with this url:" + url);
                } else {
                    LOGGER.log(Level.FINE, " Exclude pattern does not match with this url:" + url);
                }
            }
            return isExcluded;
        } catch (final Exception e) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "impossible to get URL from given input [" + url + "]:" + e);
            }
            return getExcludePattern().matcher(url).find();

        }
    }

    public Pattern getExcludePattern() {
        return excludePattern;
    }
    
}
