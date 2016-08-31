<%@ page language="java" pageEncoding="UTF-8"%>
<html>
<head>
    <title></title>
</head>

<body>
    <form action="/index" style="text-align: center ; margin-top: 150px">


        <h3>古诗词搜索</h3>
        <div style="display: block ; margin-top: 30px;margin-bottom: 10px">
            <input type="radio" name="key" value="author"> 作者
            <input type="radio" name="key" value="content" checked="checked"> 诗文
            <input type="radio" name="key" value="title"> 标题
            <input type="hidden" name="opteration" value="_search">
        </div>
        <input type="text" name="value" value="苏子与客" >
        <input type="submit" value="搜索">
    </form>




</body>
</html>
