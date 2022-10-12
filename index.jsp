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
<%

    request.setCharacterEncoding("UTF-8");

    ImageTransHelper transHelp = ImageTransHelper.getInstance ();
    CarBcdConv bcdConv = CarBcdConv.getInstance ();

    PlReq plreq = new PlReq ();

    byte [] carBcdNo = new byte [] { 0x00, 0x00, 0x00, 0x00, 0x00 };
    String carNo = request.getParameter ( "carno");

    try {

        if ( carNo == null ) carNo = "";
        else {

            carBcdNo = bcdConv.convCarNoBcdFormat ( carNo );
        }

    }
    catch ( Exception e ) {

    }

    plreq.setSeq( (short) eBrotherUtil.getIntNumber ( request.getParameter ( "seq")));
    plreq.setIcCore( (short) eBrotherUtil.getIntNumber ( request.getParameter ( "iccode")));
    plreq.setWorkNo( (short) eBrotherUtil.getIntNumber ( request.getParameter ( "workno")));
    plreq.setCarNo(  carBcdNo );

    PlResponse plresp = transHelp.initPlResponse ( plreq );

    transHelp.setPlRespDetail ( plresp );

    ByteBuffer msg_resp = transHelp.getPlRespOut ( plresp );

    byte [] msgout = new byte [ ImageTransConst._PL_RESP_HEAD_LEN_ +  ImageTransConst._PL_RESP_PAYLOAD_LEN_ ];

    msg_resp.flip();
    msg_resp.get( msgout, 0, msg_resp.capacity() );


%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

<form name="aa" action="./index.jsp" method="post" >
<table border="1">
<tr>
<td> Seq </td><td><input type="text" name="seq" value="<%= plreq.getSeq() %>" /> </td>
</tr>
<tr>
<td> ICCODE </td><td><input type="text" name="iccode" value="<%= plreq.getIcCore() %>" /> </td>
</tr>
<tr>
<td> WorkNo </td><td><input type="text" name="workno" value="<%= plreq.getWorkNo() %>" /> </td>
</tr>
<tr>
<td> 차량번호 </td><td><input type="text" name="carno" value="<%= carNo %>" /> <br/>
  <bold><%= UtilExt.print( carBcdNo ) %> </bold>
</td>
</tr>
<tr>
<td colspan="2"> <input type="submit" name="data" value="실행"/> </td>
</tr>

<tr>
<td> BL search </td><td> <%= plresp.getBlData () %></td>
</tr>

<tr>
<td> PL search </td><td> <%= plresp.getPlData () %></td>
</tr>

<tr>
<td> STPL search </td><td> <%= plresp.getStplData () %></td>
</tr>

<tr>
<td> MSG </td><td> <%= UtilExt.print( msgout ) %></td>
</tr>

</table>
</form>

<hr>

SAMPLE DATA ....

<hr>

BL
i
<br/>

<pre>


내용 : 차량번호(12)|미납금액(5)|미납건수(5)|유형(2)|처리구분(1)
         예 : 서울12가1234|21000|3|00|1
               34나5678|9000|2|00|1

유형(2)
00 : 미납(BL)
01 : 제한차량(RL)
02 : 미납&제한차량
03 : 환불
처리구분(1)
1 : 입력
2 : 수정
3 : 삭제

00거2874|500|1|00|1
00나1725|100|2|01|1
00너8027|100|3|02|1
00더8230|200|4|03|1
01가0433|4|2|00|1
01가0434|5|1|00|1
01가0463|6|1|00|1

** 차량번호 OLD 차량번호 TEST ...

서울8로1234|500|1|00|1
서울8로1235|100|2|01|1
서울8로1236|100|3|02|1
서울8로1237|200|4|03|1


01가0115|450|1|00|1
01가0122|4700|10|00|1
01가0124|55900|7|00|1
01가0195|800|1|00|1

</pre>

<hr>

PL

<pre>

차량번호(12)|유형(2)|처리구분(1)
         예 : 서울12가1234|00|1

유형(2)

00 : 경차(PL)
처리구분(1)
1 : 입력
2 : 수정
3 : 삭제


00거2874|00|1
00나1725|01|1
00너8027|02|1
00더8230|03|1


01가0115|00|1
01가0122|00|1
01가0131|00|1
01가0147|00|1
01가0155|00|1
01가0180|00|1
01가0187|00|1
01가0203|00|1
01가0204|00|1
01가0210|00|1

</pre>

<hr>

STPL ...


<pre>

차량번호(12)|유형(2)|처리구분(1)
         예 : 서울12가1234|00|1

유형(2)
00 : 소형화물(STPL)
처리구분(1)
1 : 입력
2 : 수정
3 : 삭제

00거2874|00|1
00나1725|01|1
00너8027|02|1
00더8230|03|1

강원80바1004|00
강원80바1010|00
강원80f바1019|00
강원80바1054|00
강원80바1081|00
강원80바1082|00
강원80바1083|00
강원80바1086|00
강원80바1092|00
강원80바1111|00
강원80바1119|00
강원80바1124|00
강원80바1136|00
강원80바1153|00

</pre>



</body>

</html>
