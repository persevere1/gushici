<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><html>
<head>
    <title>搜索结果 ${sessionScope.total}</title>
</head>
<style>
    .highcolor {

        color: crimson;
    }

</style>
<body>
总共: ${total}
结果为:
<c:forEach items="${sessionScope.dataList}" var="page" >

    <div style="margin-bottom: 20px">
            <div style="text-align: center">
                <span style="color: crimson;">${page.title}</span>
                ${page.author}
                ●
                ${page.dynasty}
            </div>
            <div style="text-align: center;border: 1px red double">
                ${page.content}
            </div>
            ${page.tags}

    </div>
<hr>
</c:forEach>

</body>
</html>
