/* Copyright 2006-2014 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springsecurity.web.authentication

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.web.SecurityRequestHolder

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.web.RedirectStrategy

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class AjaxAwareAuthenticationFailureHandlerTests extends GroovyTestCase {

	private final AjaxAwareAuthenticationFailureHandler handler = new AjaxAwareAuthenticationFailureHandler()
	private MockHttpServletRequest request = new MockHttpServletRequest()
	private MockHttpServletResponse response = new MockHttpServletResponse()

	void testOnAuthenticationFailureNotAjax() {

		String defaultFailureUrl = '/defaultFailureUrl'
		handler.defaultFailureUrl = defaultFailureUrl
		handler.ajaxAuthenticationFailureUrl = '/ajaxAuthenticationFailureUrl'

		boolean redirectCalled = false
		def sendRedirect = { req, res, url ->
			redirectCalled = true
			assertEquals defaultFailureUrl, url
		}
		handler.redirectStrategy = [sendRedirect: sendRedirect] as RedirectStrategy

		def config = new ConfigObject()
		config.ajaxHeader = 'ajaxHeader'
		SpringSecurityUtils.securityConfig = config

		handler.onAuthenticationFailure request, response, new BadCredentialsException('fail')
		assertTrue redirectCalled
	}

	void testOnAuthenticationFailureAjax() {

		String ajaxAuthenticationFailureUrl = '/ajaxAuthenticationFailureUrl'
		handler.defaultFailureUrl = '/defaultFailureUrl'
		handler.ajaxAuthenticationFailureUrl = ajaxAuthenticationFailureUrl

		boolean redirectCalled = false
		def sendRedirect = { req, res, url ->
			redirectCalled = true
			assertEquals ajaxAuthenticationFailureUrl, url
		}
		handler.redirectStrategy = [sendRedirect: sendRedirect] as RedirectStrategy

		def config = new ConfigObject()
		config.ajaxHeader = 'ajaxHeader'
		SpringSecurityUtils.securityConfig = config

		request.addHeader 'ajaxHeader', 'XMLHttpRequest'
		handler.onAuthenticationFailure request, response, new BadCredentialsException('fail')
		assertTrue redirectCalled
	}

	void testAfterPropertiesSet() {
		shouldFail(IllegalArgumentException) {
			handler.afterPropertiesSet()
		}

		handler.ajaxAuthenticationFailureUrl = 'url'
		handler.afterPropertiesSet()
	}

	@Override
	protected void setUp() {
		super.setUp()
		SecurityRequestHolder.set request, response
	}

	/**
	 * {@inheritDoc}
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() {
		super.tearDown()
		SpringSecurityUtils.resetSecurityConfig()
		org.codehaus.groovy.grails.commons.ConfigurationHolder.config = null
		SecurityRequestHolder.reset()
	}
}
