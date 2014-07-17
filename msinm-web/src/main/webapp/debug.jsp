<%@ page import="java.util.Enumeration" %>
<html>
<head>
    <title>Debug</title>
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Expires" content="0"/>
</head>

<body>

    <h2>Request Attributes</h2>

    <ul>
        <li>URL: <%= request.getRequestURL() %></li>
        <li>URI: <%= request.getRequestURI() %></li>
        <li>Secure: <%= request.isSecure() %></li>
        <li>Scheme: <%= request.getScheme() %></li>
        <li>Remote Addr: <%= request.getRemoteAddr() %></li>
        <li>Remote Host: <%= request.getRemoteHost() %></li>
        <li>Remote Port: <%= request.getRemotePort() %></li>
        <li>Local Addr: <%= request.getLocalAddr() %></li>
        <li>Server name: <%= request.getServerName() %></li>
        <li>Server port: <%= request.getServerPort() %></li>
        <li>Headers:<br>
        <%
            StringBuilder str = new StringBuilder();
            for (Enumeration<String> it = request.getHeaderNames(); it.hasMoreElements(); ) {
                String name = it.nextElement();
                String value = request.getHeader(name);
                %>&nbsp;&nbsp;&nbsp;<b><%= name %>:</b> <%= value %><br><%
            }
        %>
        </li>
    </ul>

</body>
</html>