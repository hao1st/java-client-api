/*
 * Copyright 2018-2019 MarkLogic Corporation
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
package com.marklogic.client.impl;

import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * ClientCookie is a wrapper around the Cookie implementation so that the
 * underlying implementation can be changed.
 *
 */
public class ClientCookie {
  Cookie cookie;

  ClientCookie(String name, String value, long expiresAt, String domain, String path,
      boolean secure) {
    Cookie.Builder cookieBldr = new Cookie.Builder()
           .domain(domain)
           .path(path)
           .name(name)
           .value(value)
           .expiresAt(expiresAt);
    if ( secure == true ) cookieBldr = cookieBldr.secure();
    this.cookie = cookieBldr.build();
  }

  public ClientCookie(ClientCookie cookie) {
    this(cookie.getName(), cookie.getValue(), cookie.expiresAt(), cookie.getDomain(), cookie.getPath(),
        cookie.isSecure());
  }

  public boolean isSecure() {
    return cookie.secure();
  }

  public String getPath() {
    return cookie.path();
  }

  public String getDomain() {
    return cookie.domain();
  }

  public long expiresAt() {
    return cookie.expiresAt();
  }

  public String getName() {
    return cookie.name();
  }

  public int getMaxAge() {
    return (int) TimeUnit.MILLISECONDS.toSeconds(cookie.expiresAt() - System.currentTimeMillis());
  }
  public String getValue() {
    return cookie.value();
  }

  public static ClientCookie parse(HttpUrl url, String setCookie) {
    Cookie cookie = Cookie.parse(url, setCookie);
    if(cookie == null) throw new IllegalStateException(setCookie + "is not a well-formed cookie");
    return new ClientCookie(cookie.name(), cookie.value(), cookie.expiresAt(), cookie.domain(), cookie.path(),
        cookie.secure());
  }

  public String toString() {
    return cookie.toString();
  }
}
