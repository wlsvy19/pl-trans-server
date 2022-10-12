<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.nio.ByteBuffer" %>
<%@ page import="com.eBrother.trans.image.util.CarBcdConv" %>
<%@ page import="com.eBrother.trans.image.ImageTransHelper" %>
<%@ page import="com.eBrother.trans.image.ImageTransConst" %>
<%@ page import="com.eBrother.trans.image.model.PlResponse" %>
<%@ page import="com.eBrother.trans.image.model.PlReq" %>
<%@ page import="com.eBrother.util.eBrotherUtil" %>
<%@ page import="com.eBrother.util.UtilExt" %>

<%@ page import = "java.io.*,java.util.*, javax.servlet.*" %>
<%@ page import = "javax.servlet.http.*" %>
<%@ page import = "org.apache.commons.fileupload.*" %>
<%@ page import = "org.apache.commons.fileupload.disk.*" %>
<%@ page import = "org.apache.commons.fileupload.servlet.*" %>
<%@ page import = "org.apache.commons.io.output.*" %>
<html>
   <head>
      <title>File Uploading Form</title>
   </head>

   <body>
      <h3>File Upload:</h3>
      Select a file to upload: <br />
      <form action = "UploadServlet" method = "post"  enctype = "multipart/form-data">
         <input type = "file" name = "file" size = "50" />
         <br />
         <input type = "submit" value = "Upload File" />
      </form>
   </body>

</html>