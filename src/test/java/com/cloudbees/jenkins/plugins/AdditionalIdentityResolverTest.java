/*
 * The MIT License
 *
 *  Copyright (c) 2012, CloudBees, Inc.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.cloudbees.jenkins.plugins;

import static org.junit.Assert.*;

import hudson.model.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Integration tests for {@link AdditionalIdentityResolver}.
 */
public class AdditionalIdentityResolverTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private AdditionalIdentityResolver resolver;
    private User testUser;

    @Before
    public void setUp() throws Exception {
        resolver = new AdditionalIdentityResolver();
        testUser = User.getById("testuser", true);
    }

    @Test
    public void testResolveCanonicalIdWithoutRealm() throws IOException {
        // Create and set up identities
        List<AdditionalIdentity> identities = new ArrayList<>();
        identities.add(new AdditionalIdentity("github-user", "github"));
        identities.add(new AdditionalIdentity("ldap-user", "ldap"));

        AdditionalIdentities userIdentities = new AdditionalIdentities(identities);
        testUser.addProperty(userIdentities);

        // Test resolving without realm constraint
        String resolved = resolver.resolveCanonicalId("github-user", null);
        assertEquals("Should resolve to the test user ID", testUser.getId(), resolved);

        // Test with non-existent ID
        assertNull("Non-existent ID should resolve to null", resolver.resolveCanonicalId("non-existent", null));
    }

    @Test
    public void testResolveCanonicalIdWithRealm() throws IOException {
        // Create and set up identities
        List<AdditionalIdentity> identities = new ArrayList<>();
        identities.add(new AdditionalIdentity("github-user", "github"));
        identities.add(new AdditionalIdentity("ldap-user", "ldap"));

        AdditionalIdentities userIdentities = new AdditionalIdentities(identities);
        testUser.addProperty(userIdentities);

        // Create realm context
        Map<String, Object> context = new HashMap<>();
        context.put(User.CanonicalIdResolver.REALM, "github");

        // Test resolving with matching realm
        String resolved = resolver.resolveCanonicalId("github-user", context);
        assertEquals("Should resolve to the test user ID with matching realm", testUser.getId(), resolved);

        // Test resolving with non-matching realm
        context.put(User.CanonicalIdResolver.REALM, "bitbucket");
        resolved = resolver.resolveCanonicalId("github-user", context);
        assertNull("Should not resolve with non-matching realm", resolved);
    }

    @Test
    public void testAddAliases() {
        // This just verifies that the method doesn't throw exceptions
        AdditionalIdentityResolver.addAliases();
        // No assertions needed, we're just checking that it doesn't crash
    }
}
