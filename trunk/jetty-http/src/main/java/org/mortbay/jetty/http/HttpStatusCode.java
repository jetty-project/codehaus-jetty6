// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.http;

import org.mortbay.jetty.io.Buffer;
import org.mortbay.jetty.io.ByteArrayBuffer;
import org.mortbay.jetty.util.TypeUtil;

/**
 * <p>
 * HttpStatusCode enum class, for status codes based on various HTTP RFCs. (see table below)
 * </p>
 * 
 * <table border="1" cellpadding="5">
 *   <tr>
 *     <th>Enum</th>
 *     <th>Ordinal</th>
 *     <th>Message</th>
 *     <th>
 *       <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>
 *     </th>
 *     <th>
 *       <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>
 *     </th>
 *     <th>
 *       <a href="http://tools.ietf.org/html/rfc2518">RFC 2518 - WEBDAV</a>
 *     </th>
 *   </tr>
 *   
 *   <tr>
 *     <td><strong><code>Informational - 1xx</code></strong></td>
 *     <td colspan="5">{@link #isInformational(int)}</td>
 *   </tr>
 *   
 *   <tr>
 *     <td>{@link #CONTINUE}</td>
 *     <td>100</td>
 *     <td>Continue</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.1.1">Sec. 10.1.1</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #SWITCHING_PROTOCOLS}</td>
 *     <td>101</td>
 *     <td>Switching Protocols</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.1.2">Sec. 10.1.2</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #PROCESSING}</td>
 *     <td>102</td>
 *     <td>Processing</td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2518#section-10.1">Sec. 10.1</a>
 *     </td>
 *   </tr>
 *   
 *   <tr>
 *     <td><strong><code>Success - 2xx</code></strong></td>
 *     <td colspan="5">{@link #isSuccess(int)}</td>
 *   </tr>
 *   
 *   <tr>
 *     <td>{@link #OK}</td>
 *     <td>200</td>
 *     <td>OK</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.2">Sec. 9.2</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.2.1">Sec. 10.2.1</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #CREATED}</td>
 *     <td>201</td>
 *     <td>Created</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.2">Sec. 9.2</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.2.2">Sec. 10.2.2</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #ACCEPTED}</td>
 *     <td>202</td>
 *     <td>Accepted</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.2">Sec. 9.2</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.2.3">Sec. 10.2.3</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #NON_AUTHORITATIVE_INFORMATION}</td>
 *     <td>203</td>
 *     <td>Non Authoritative Information</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.2.4">Sec. 10.2.4</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #NO_CONTENT}</td>
 *     <td>204</td>
 *     <td>No Content</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.2">Sec. 9.2</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.2.5">Sec. 10.2.5</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #RESET_CONTENT}</td>
 *     <td>205</td>
 *     <td>Reset Content</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.2.6">Sec. 10.2.6</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #PARTIAL_CONTENT}</td>
 *     <td>206</td>
 *     <td>Partial Content</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.2.7">Sec. 10.2.7</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #MULTI_STATUS}</td>
 *     <td>207</td>
 *     <td>Multi-Status</td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2518#section-10.2">Sec. 10.2</a>
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td>
 *     <td><strike>207</strike></td>
 *     <td><strike>Partial Update OK</strike></td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-rev-01.txt">draft/01</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   
 *   <tr>
 *     <td><strong><code>Redirection - 3xx</code></strong></td>
 *     <td colspan="5">{@link #isRedirection(int)}</td>
 *   </tr>
 *   
 *   <tr>
 *     <td>{@link #MULTIPLE_CHOICES}</td>
 *     <td>300</td>
 *     <td>Multiple Choices</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.3">Sec. 9.3</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.3.1">Sec. 10.3.1</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #MOVED_PERMANENTLY}</td>
 *     <td>301</td>
 *     <td>Moved Permanently</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.3">Sec. 9.3</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.3.2">Sec. 10.3.2</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #MOVED_TEMPORARILY}</td>
 *     <td>302</td>
 *     <td>Moved Temporarily</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.3">Sec. 9.3</a> 
 *     </td>
 *     <td>(now "<code>302 Found</code>")</td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #FOUND}</td>
 *     <td>302</td>
 *     <td>Found</td>
 *     <td>(was "<code>302 Moved Temporarily</code>")</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.3.3">Sec. 10.3.3</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #SEE_OTHER}</td>
 *     <td>303</td>
 *     <td>See Other</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.3.4">Sec. 10.3.4</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #NOT_MODIFIED}</td>
 *     <td>304</td>
 *     <td>Not Modified</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.3">Sec. 9.3</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.3.5">Sec. 10.3.5</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #USE_PROXY}</td>
 *     <td>305</td>
 *     <td>Use Proxy</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.3.6">Sec. 10.3.6</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td>
 *     <td>306</td>
 *     <td><em>(Unused)</em></td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.3.7">Sec. 10.3.7</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #TEMPORARY_REDIRECT}</td>
 *     <td>307</td>
 *     <td>Temporary Redirect</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.3.8">Sec. 10.3.8</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   
 *   <tr>
 *     <td><strong><code>Client Error - 4xx</code></strong></td>
 *     <td colspan="5">{@link #isClientError(int)}</td>
 *   </tr>
 *   
 *   <tr>
 *     <td>{@link #BAD_REQUEST}</td>
 *     <td>400</td>
 *     <td>Bad Request</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.4">Sec. 9.4</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.1">Sec. 10.4.1</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #UNAUTHORIZED}</td>
 *     <td>401</td>
 *     <td>Unauthorized</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.4">Sec. 9.4</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.2">Sec. 10.4.2</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #PAYMENT_REQUIRED}</td>
 *     <td>402</td>
 *     <td>Payment Required</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.4">Sec. 9.4</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.3">Sec. 10.4.3</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #FORBIDDEN}</td>
 *     <td>403</td>
 *     <td>Forbidden</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.4">Sec. 9.4</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.4">Sec. 10.4.4</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #NOT_FOUND}</td>
 *     <td>404</td>
 *     <td>Not Found</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.4">Sec. 9.4</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.5">Sec. 10.4.5</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #METHOD_NOT_ALLOWED}</td>
 *     <td>405</td>
 *     <td>Method Not Allowed</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.6">Sec. 10.4.6</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #NOT_ACCEPTABLE}</td>
 *     <td>406</td>
 *     <td>Not Acceptable</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.7">Sec. 10.4.7</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #PROXY_AUTHENTICATION_REQUIRED}</td>
 *     <td>407</td>
 *     <td>Proxy Authentication Required</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.8">Sec. 10.4.8</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #REQUEST_TIMEOUT}</td>
 *     <td>408</td>
 *     <td>Request Timeout</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.9">Sec. 10.4.9</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #CONFLICT}</td>
 *     <td>409</td>
 *     <td>Conflict</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.10">Sec. 10.4.10</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #GONE}</td>
 *     <td>410</td>
 *     <td>Gone</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.11">Sec. 10.4.11</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #LENGTH_REQUIRED}</td>
 *     <td>411</td>
 *     <td>Length Required</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.12">Sec. 10.4.12</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #PRECONDITION_FAILED}</td>
 *     <td>412</td>
 *     <td>Precondition Failed</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.13">Sec. 10.4.13</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #REQUEST_ENTITY_TOO_LARGE}</td>
 *     <td>413</td>
 *     <td>Request Entity Too Large</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.14">Sec. 10.4.14</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #REQUEST_URI_TOO_LONG}</td>
 *     <td>414</td>
 *     <td>Request-URI Too Long</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.15">Sec. 10.4.15</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #UNSUPPORTED_MEDIA_TYPE}</td>
 *     <td>415</td>
 *     <td>Unsupported Media Type</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.16">Sec. 10.4.16</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #REQUESTED_RANGE_NOT_SATISFIABLE}</td>
 *     <td>416</td>
 *     <td>Requested Range Not Satisfiable</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.17">Sec. 10.4.17</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #EXPECTATION_FAILED}</td>
 *     <td>417</td>
 *     <td>Expectation Failed</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.4.18">Sec. 10.4.18</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td>
 *     <td><strike>418</strike></td>
 *     <td><strike>Reauthentication Required</strike></td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/draft-ietf-http-v11-spec-rev-01#section-10.4.19">draft/01</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td>
 *     <td><strike>418</strike></td>
 *     <td><strike>Unprocessable Entity</strike></td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/draft-ietf-webdav-protocol-05#section-10.3">draft/05</a>
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td>
 *     <td><strike>419</strike></td>
 *     <td><strike>Proxy Reauthentication Required</stike></td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/draft-ietf-http-v11-spec-rev-01#section-10.4.20">draft/01</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td>
 *     <td><strike>419</strike></td>
 *     <td><strike>Insufficient Space on Resource</stike></td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/draft-ietf-webdav-protocol-05#section-10.4">draft/05</a>
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td>
 *     <td><strike>420</strike></td>
 *     <td><strike>Method Failure</strike></td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/draft-ietf-webdav-protocol-05#section-10.5">draft/05</a>
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td>
 *     <td>421</td>
 *     <td><em>(Unused)</em></td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #UNPROCESSABLE_ENTITY}</td>
 *     <td>422</td>
 *     <td>Unprocessable Entity</td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2518#section-10.3">Sec. 10.3</a>
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>{@link #LOCKED}</td>
 *     <td>423</td>
 *     <td>Locked</td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2518#section-10.4">Sec. 10.4</a>
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>{@link #FAILED_DEPENDENCY}</td>
 *     <td>424</td>
 *     <td>Failed Dependency</td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2518#section-10.5">Sec. 10.5</a>
 *     </td>
 *   </tr>
 *   
 *   <tr>
 *     <td><strong><code>Server Error - 5xx</code></strong></td>
 *     <td colspan="5">{@link #isServerError(int)}</td>
 *   </tr>
 *   
 *   <tr>
 *     <td>{@link #INTERNAL_SERVER_ERROR}</td>
 *     <td>500</td>
 *     <td>Internal Server Error</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.5">Sec. 9.5</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.5.1">Sec. 10.5.1</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #NOT_IMPLEMENTED}</td>
 *     <td>501</td>
 *     <td>Not Implemented</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.5">Sec. 9.5</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.5.2">Sec. 10.5.2</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #BAD_GATEWAY}</td>
 *     <td>502</td>
 *     <td>Bad Gateway</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.5">Sec. 9.5</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.5.3">Sec. 10.5.3</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #SERVICE_UNAVAILABLE}</td>
 *     <td>503</td>
 *     <td>Service Unavailable</td>
 *     <td>
 *     	 <a href="http://tools.ietf.org/html/rfc1945#section-9.5">Sec. 9.5</a> 
 *     </td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.5.4">Sec. 10.5.4</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #GATEWAY_TIMEOUT}</td>
 *     <td>504</td>
 *     <td>Gateway Timeout</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.5.5">Sec. 10.5.5</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #HTTP_VERSION_NOT_SUPPORTED}</td>
 *     <td>505</td>
 *     <td>HTTP Version Not Supported</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2616#section-10.5.6">Sec. 10.5.6</a>
 *     </td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td>
 *     <td>506</td>
 *     <td><em>(Unused)</em></td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #INSUFFICIENT_STORAGE}</td>
 *     <td>507</td>
 *     <td>Insufficient Storage</td>
 *     <td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td>
 *       <a href="http://tools.ietf.org/html/rfc2518#section-10.6">Sec. 10.6</a>
 *     </td>
 *   </tr>
 *   
 * </table>
 * 
 * @version $Id$
 */
public enum HttpStatusCode
{

    /* --------------------------------------------------------------------
     * Informational messages in 1xx series.
     * As defined by ...
     *   RFC 1945 - HTTP/1.0
     *   RFC 2616 - HTTP/1.1
     *   RFC 2518 - WebDAV
     */

    /** <code>100 Continue</code> */
    CONTINUE(100, "Continue"),
    /** <code>101 Switching Protocols</code> */
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    /** <code>102 Processing</code> */
    PROCESSING(102, "Processing"),

    /* --------------------------------------------------------------------
     * Success messages in 2xx series.
     * As defined by ...
     *   RFC 1945 - HTTP/1.0
     *   RFC 2616 - HTTP/1.1
     *   RFC 2518 - WebDAV
     */

    /** <code>200 OK</code> */
    OK(200, "OK"),
    /** <code>201 Created</code> */
    CREATED(201, "Created"),
    /** <code>202 Accepted</code> */
    ACCEPTED(202, "Accepted"),
    /** <code>203 Non Authoritative Information</code> */
    NON_AUTHORITATIVE_INFORMATION(203, "Non Authoritative Information"),
    /** <code>204 No Content</code> */
    NO_CONTENT(204, "No Content"),
    /** <code>205 Reset Content</code> */
    RESET_CONTENT(205, "Reset Content"),
    /** <code>206 Partial Content</code> */
    PARTIAL_CONTENT(206, "Partial Content"),
    /** <code>207 Multi-Status</code> */
    MULTI_STATUS(207, "Multi-Status"),

    /* --------------------------------------------------------------------
     * Redirection messages in 3xx series.
     * As defined by ...
     *   RFC 1945 - HTTP/1.0
     *   RFC 2616 - HTTP/1.1
     */

    /** <code>300 Mutliple Choices</code> */
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    /** <code>301 Moved Permanently</code> */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /** <code>302 Moved Temporarily</code> */
    MOVED_TEMPORARILY(302, "Moved Temporarily"),
    /** <code>302 Found</code> */
    FOUND(302, "Found"),
    /** <code>303 See Other</code> */
    SEE_OTHER(303, "See Other"),
    /** <code>304 Not Modified</code> */
    NOT_MODIFIED(304, "Not Modified"),
    /** <code>305 Use Proxy</code> */
    USE_PROXY(305, "Use Proxy"),
    /** <code>307 Temporary Redirect</code> */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),

    /* --------------------------------------------------------------------
     * Client Error messages in 4xx series.
     * As defined by ...
     *   RFC 1945 - HTTP/1.0
     *   RFC 2616 - HTTP/1.1
     *   RFC 2518 - WebDAV
     */

    /** <code>400 Bad Request</code> */
    BAD_REQUEST(400, "Bad Request"),
    /** <code>401 Unauthorized</code> */
    UNAUTHORIZED(401, "Unauthorized"),
    /** <code>402 Payment Required</code> */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /** <code>403 Forbidden</code> */
    FORBIDDEN(403, "Forbidden"),
    /** <code>404 Not Found</code> */
    NOT_FOUND(404, "Not Found"),
    /** <code>405 Method Not Allowed</code> */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /** <code>406 Not Acceptable</code> */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /** <code>407 Proxy Authentication Required</code>*/
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    /** <code>408 Request Timeout</code> */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /** <code>409 Conflict</code> */
    CONFLICT(409, "Conflict"),
    /** <code>410 Gone</code> */
    GONE(410, "Gone"),
    /** <code>411 Length Required</code> */
    LENGTH_REQUIRED(411, "Length Required"),
    /** <code>412 Precondition Failed</code> */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /** <code>413 Request Entity Too Large</code> */
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    /** <code>414 Request-URI Too Long</code> */
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    /** <code>415 Unsupported Media Type</code> */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /** <code>416 Requested Range Not Satisfiable</code> */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    /** <code>417 Expectation Failed</code> */
    EXPECTATION_FAILED(417, "Expectation Failed"),
    /** <code>422 Unprocessable Entity</code> */
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    /** <code>423 Locked</code> */
    LOCKED(423, "Locked"),
    /** <code>424 Failed Dependency</code> */
    FAILED_DEPENDENCY(424, "Failed Dependency"),

    /* --------------------------------------------------------------------
     * Server Error messages in 5xx series.
     * As defined by ...
     *   RFC 1945 - HTTP/1.0
     *   RFC 2616 - HTTP/1.1
     *   RFC 2518 - WebDAV
     */

    /** <code>500 Server Error</code> */
    INTERNAL_SERVER_ERROR(500, "Server Error"),
    /** <code>501 Not Implemented</code> */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /** <code>502 Bad Gateway</code> */
    BAD_GATEWAY(502, "Bad Gateway"),
    /** <code>503 Service Unavailable</code> */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /** <code>504 Gateway Timeout</code> */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /** <code>505 HTTP Version Not Supported</code> */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    /** <code>507 Insufficient Storage</code> */
    INSUFFICIENT_STORAGE(507, "Insufficient Storage");

    private static final int ORDINAL_MAX = 508;
    private int ordinal;
    private Buffer messageBuffer;
    private Buffer responseBuffer;

    private HttpStatusCode(int ordinal, String message)
    {
        this.ordinal = ordinal;
        this.messageBuffer = new ByteArrayBuffer(message.getBytes());
        this.responseBuffer = new ByteArrayBuffer(String.format("%03d %s", ordinal, message).getBytes());
    }

    public int getOrdinal()
    {
        return ordinal;
    }

    public String getMessage()
    {
        return messageBuffer.toString();
    }

    public Buffer getMessageBuffer()
    {
        return messageBuffer;
    }

    private static HttpStatusCode codeMap[] = new HttpStatusCode[ORDINAL_MAX];

    static
    {
        for (HttpStatusCode code : values())
        {
            codeMap[code.ordinal] = code;
        }
    }

    public boolean equals(int ordinal)
    {
        return (this.ordinal == ordinal);
    }

    @Override
    public String toString()
    {
        return String.format("[%03d %s]", this.ordinal, this.getMessage());
    }

    /**
     * Get the status message for a specific ordinal.
     * 
     * @param ordinal the ordinal to look up
     * @return the specific message, or the ordinal number itself if ordinal does not match known list.
     */
    public static String getMessage(int ordinal)
    {
        HttpStatusCode code = getCode(ordinal);
        if (code != null)
        {
            return code.getMessage();
        } else
        {
            return TypeUtil.toString(ordinal);
        }
    }

    /**
     * Get the status message {@link Buffer} for a specific ordinal.
     * 
     * @param ordinal the ordinal to look up
     * @return the specific message {@link Buffer}, or null if ordinal does not match known list.
     */
    public static Buffer getMessageBuffer(int ordinal)
    {
        HttpStatusCode code = getCode(ordinal);
        if (code == null)
        {
            return null;
        }
        return code.getMessageBuffer();
    }

    /**
     * Get the HttpStatusCode for a specific ordinal
     * 
     * @param ordinal the ordinal to lookup.
     * @return the {@link HttpStatusCode} if found, or null if not found.
     * TODO: should we create a code called "UNKNOWN" ??
     */
    public static HttpStatusCode getCode(int ordinal)
    {
        if (ordinal < ORDINAL_MAX)
        {
            return codeMap[ordinal];
        }
        return null;
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Informational</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @param ordinal the ordinal to test.
     * @return true if within range of codes that belongs to <code>Informational</code> messages.
     */
    public static boolean isInformational(int ordinal)
    {
        return ((100 <= ordinal) && (ordinal <= 199));
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Informational</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @return true if within range of codes that belongs to <code>Informational</code> messages.
     */
    public boolean isInformational()
    {
        return HttpStatusCode.isInformational(this.ordinal);
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Success</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @param ordinal the ordinal to test.
     * @return true if within range of codes that belongs to <code>Success</code> messages.
     */
    public static boolean isSuccess(int ordinal)
    {
        return ((200 <= ordinal) && (ordinal <= 299));
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Success</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @return true if within range of codes that belongs to <code>Success</code> messages.
     */
    public boolean isSuccess()
    {
        return HttpStatusCode.isSuccess(this.ordinal);
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Redirection</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @param ordinal the ordinal to test.
     * @return true if within range of codes that belongs to <code>Redirection</code> messages.
     */
    public static boolean isRedirection(int ordinal)
    {
        return ((300 <= ordinal) && (ordinal <= 399));
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Redirection</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @return true if within range of codes that belongs to <code>Redirection</code> messages.
     */
    public boolean isRedirection()
    {
        return HttpStatusCode.isRedirection(this.ordinal);
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Client Error</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @param ordinal the ordinal to test.
     * @return true if within range of codes that belongs to <code>Client Error</code> messages.
     */
    public static boolean isClientError(int ordinal)
    {
        return ((400 <= ordinal) && (ordinal <= 499));
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Client Error</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @return true if within range of codes that belongs to <code>Client Error</code> messages.
     */
    public boolean isClientError()
    {
        return HttpStatusCode.isClientError(this.ordinal);
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Server Error</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @param ordinal the ordinal to test.
     * @return true if within range of codes that belongs to <code>Server Error</code> messages.
     */
    public static boolean isServerError(int ordinal)
    {
        return ((500 <= ordinal) && (ordinal <= 599));
    }

    /**
     * Simple test against an ordinal to determine if it falls into the
     * <code>Server Error</code> message category as defined in the 
     * <a href="http://tools.ietf.org/html/rfc1945">RFC 1945 - HTTP/1.0</a>, and 
     * <a href="http://tools.ietf.org/html/rfc2616">RFC 2616 - HTTP/1.1</a>.
     * 
     * @return true if within range of codes that belongs to <code>Server Error</code> messages.
     */
    public boolean isServerError()
    {
        return HttpStatusCode.isServerError(this.ordinal);
    }

    /**
     * Return a response line, suitable for HTTP protocol responses.
     * 
     * @param ordinal the ordinal to look up.
     * @return the response line as a Buffer
     */
    public static Buffer getResponseLine(int ordinal)
    {
        HttpStatusCode code = getCode(ordinal);
        if (code == null)
        {
            return null;
        }
        return code.responseBuffer;
    }

    /**
     * Return a response line, suitable for HTTP protocol responses.
     * 
     * @return the response linen as a Buffer
     */
    public Buffer getResponseLine()
    {
        return this.responseBuffer;
    }
}
