package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import java.util.*;
import java.nio.ByteBuffer;
import com.eBrother.trans.image.util.CarBcdConv;
import com.eBrother.trans.image.ImageTransHelper;
import com.eBrother.trans.image.ImageTransConst;
import com.eBrother.trans.image.model.PlResponse;
import com.eBrother.trans.image.model.PlReq;
import com.eBrother.util.eBrotherUtil;
import com.eBrother.util.UtilExt;

public final class index_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.AnnotationProcessor _jsp_annotationprocessor;

  public Object getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_annotationprocessor = (org.apache.AnnotationProcessor) getServletConfig().getServletContext().getAttribute(org.apache.AnnotationProcessor.class.getName());
  }

  public void _jspDestroy() {
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html; charset=UTF-8");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");


    request.setCharacterEncoding("UTF-8");

    ImageTransHelper transHelp = ImageTransHelper.getInstance ();
    CarBcdConv bcdConv = CarBcdConv.getInstance ();

    PlReq plreq = new PlReq ();
    PlResponse plresp = null;

    byte [] carBcdNo = new byte [] { 0x00, 0x00, 0x00, 0x00, 0x00 };
    String carNo = request.getParameter ( "carno");
    byte [] msgout  = null;

    int callType =  eBrotherUtil.getIntNumber (request.getParameter ( "callType"));

    if ( callType == 0 ) callType = ImageTransConst._PL_MSG_REQ_;

    callType = ImageTransConst._PL_MSG_REQ_;

    if ( callType == ImageTransConst._PL_MSG_REQ_ ) {

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

        plresp = transHelp.initPlResponse ( plreq, ImageTransConst._PL_MSG_RESP_, ImageTransConst._PL_RESP_PAYLOAD_LEN_ );


        transHelp.setPlRespDetail ( plreq, plresp );

        ByteBuffer msg_resp = transHelp.getPlRespOut ( plreq, plresp );

        msgout = new byte [ ImageTransConst._PL_RESP_HEAD_LEN_V2 +  ImageTransConst._PL_RESP_PAYLOAD_LEN_ ];

        msg_resp.flip();
        msg_resp.get( msgout, 0, msg_resp.capacity() );
    }



      out.write("\n");
      out.write("\n");
      out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
      out.write("<html>\n");
      out.write("<head>\n");
      out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
      out.write("<title>Insert title here</title>\n");
      out.write("</head>\n");
      out.write("<body>\n");
      out.write("\n");
      out.write("<hr/>\n");
      out.write("<form name=\"aa\" action=\"./index.jsp\" method=\"post\" >\n");
      out.write("<table border=\"1\">\n");
      out.write("<tr>\n");
      out.write("<td> 전문 Type </td><td><input type=\"text\" name=\"callType\" value=\"");
      out.print( callType );
      out.write("\" /> </td>\n");
      out.write("</tr>\n");
      out.write("<tr>\n");
      out.write("<td> Seq </td><td><input type=\"text\" name=\"seq\" value=\"");
      out.print( plreq.getSeq() );
      out.write("\" /> </td>\n");
      out.write("</tr>\n");
      out.write("<tr>\n");
      out.write("<td> ICCODE </td><td><input type=\"text\" name=\"iccode\" value=\"");
      out.print( plreq.getIcCore() );
      out.write("\" /> </td>\n");
      out.write("</tr>\n");
      out.write("<tr>\n");
      out.write("<td> WorkNo </td><td><input type=\"text\" name=\"workno\" value=\"");
      out.print( plreq.getWorkNo() );
      out.write("\" /> </td>\n");
      out.write("</tr>\n");
      out.write("<tr>\n");
      out.write("<td> 차량번호 </td><td><input type=\"text\" name=\"carno\" value=\"");
      out.print( carNo );
      out.write("\" /> <br/>\n");
      out.write("  <bold>");
      out.print( UtilExt.print( carBcdNo ) );
      out.write(" </bold>\n");
      out.write("</td>\n");
      out.write("</tr>\n");
      out.write("<tr>\n");
      out.write("<td colspan=\"2\"> <input type=\"submit\" name=\"data\" value=\"실행\"/> </td>\n");
      out.write("</tr>\n");
      out.write("\n");
      out.write("<tr>\n");
      out.write("<td> 차량제원정보 </td><td> ");
      out.print( plresp.getCsData () );
      out.write("</td>\n");
      out.write("</tr>\n");
      out.write("\n");
      out.write("\n");
      out.write("export JAVA_HOME=/usr/lib/jvm/jre1.8.0_231\n");
      out.write("export PATH=$JAVA_HOME/bin:$PATH\n");
      out.write("\n");
      out.write("cd ~/apiservice/data\n");
      out.write(" rm -f ./backup/*.tmp\n");
      out.write("\n");
      out.write("mv ./backup/*20201023* ./in\n");
      out.write("\n");
      out.write("cd ~/apiservice/pl-trans-server/bin\n");
      out.write("\n");
      out.write("\n");
      out.write("<tr>\n");
      out.write("<td> BL search </td><td> ");
      out.print( plresp.getBlData () );
      out.write("</td>\n");
      out.write("</tr>\n");
      out.write("\n");
      out.write("<tr>\n");
      out.write("<td> STPL search </td><td> ");
      out.print( plresp.getStplData () );
      out.write("</td>\n");
      out.write("</tr>\n");
      out.write("\n");
      out.write("<tr>\n");
      out.write("<td> PL search </td><td> ** 소형차량정보 처리 여부 미확정 - 차량제원정보에서 차종 ( 1 ~ 6 ) 에 없으면, PL 로 처리  ");
      out.print( plresp.getPlData () );
      out.write("</td>\n");
      out.write("</tr>\n");
      out.write("\n");
      out.write("<tr>\n");
      out.write("<td> MSG </td><td> ");
      out.print( UtilExt.print( msgout ) );
      out.write("</td>\n");
      out.write("</tr>\n");
      out.write("\n");
      out.write("</table>\n");
      out.write("</form>\n");
      out.write("\n");
      out.write("<hr>\n");
      out.write("\n");
      out.write("SAMPLE DATA ....\n");
      out.write("\n");
      out.write("<hr>\n");
      out.write("\n");
      out.write("BL\n");
      out.write("i\n");
      out.write("<br/>\n");
      out.write("\n");
      out.write("<pre>\n");
      out.write("\n");
      out.write("\n");
      out.write("내용 : 차량번호(12)|미납금액(5)|미납건수(5)|유형(2)|처리구분(1)\n");
      out.write("         예 : 서울12가1234|21000|3|00|1\n");
      out.write("               34나5678|9000|2|00|1\n");
      out.write("\n");
      out.write("유형(2)\n");
      out.write("00 : 미납(BL)\n");
      out.write("01 : 제한차량(RL)\n");
      out.write("02 : 미납&제한차량\n");
      out.write("03 : 환불\n");
      out.write("처리구분(1)\n");
      out.write("1 : 입력\n");
      out.write("2 : 수정\n");
      out.write("3 : 삭제\n");
      out.write("\n");
      out.write("00거2874|500|1|00|1\n");
      out.write("00나1725|100|2|01|1\n");
      out.write("00너8027|100|3|02|1\n");
      out.write("00더8230|200|4|03|1\n");
      out.write("01가0433|4|2|00|1\n");
      out.write("01가0434|5|1|00|1\n");
      out.write("01가0463|6|1|00|1\n");
      out.write("\n");
      out.write("** 차량번호 OLD 차량번호 TEST ...\n");
      out.write("\n");
      out.write("서울8로1234|500|1|00|1\n");
      out.write("서울8로1235|100|2|01|1\n");
      out.write("서울8로1236|100|3|02|1\n");
      out.write("서울8로1237|200|4|03|1\n");
      out.write("\n");
      out.write("\n");
      out.write("01가0115|450|1|00|1\n");
      out.write("01가0122|4700|10|00|1\n");
      out.write("01가0124|55900|7|00|1\n");
      out.write("01가0195|800|1|00|1\n");
      out.write("\n");
      out.write("</pre>\n");
      out.write("\n");
      out.write("<hr>\n");
      out.write("\n");
      out.write("PL\n");
      out.write("\n");
      out.write("<pre>\n");
      out.write("\n");
      out.write("차량번호(12)|유형(2)|처리구분(1)\n");
      out.write("         예 : 서울12가1234|00|1\n");
      out.write("\n");
      out.write("유형(2)\n");
      out.write("\n");
      out.write("00 : 경차(PL)\n");
      out.write("처리구분(1)\n");
      out.write("1 : 입력\n");
      out.write("2 : 수정\n");
      out.write("3 : 삭제\n");
      out.write("\n");
      out.write("\n");
      out.write("00거2874|00|1\n");
      out.write("00나1725|01|1\n");
      out.write("00너8027|02|1\n");
      out.write("00더8230|03|1\n");
      out.write("\n");
      out.write("\n");
      out.write("01가0115|00|1\n");
      out.write("01가0122|00|1\n");
      out.write("01가0131|00|1\n");
      out.write("01가0147|00|1\n");
      out.write("01가0155|00|1\n");
      out.write("01가0180|00|1\n");
      out.write("01가0187|00|1\n");
      out.write("01가0203|00|1\n");
      out.write("01가0204|00|1\n");
      out.write("01가0210|00|1\n");
      out.write("\n");
      out.write("</pre>\n");
      out.write("\n");
      out.write("<hr>\n");
      out.write("\n");
      out.write("STPL ...\n");
      out.write("\n");
      out.write("\n");
      out.write("<pre>\n");
      out.write("\n");
      out.write("차량번호(12)|유형(2)|처리구분(1)\n");
      out.write("         예 : 서울12가1234|00|1\n");
      out.write("\n");
      out.write("유형(2)\n");
      out.write("00 : 소형화물(STPL)\n");
      out.write("처리구분(1)\n");
      out.write("1 : 입력\n");
      out.write("2 : 수정\n");
      out.write("3 : 삭제\n");
      out.write("\n");
      out.write("00거2874|00|1\n");
      out.write("00나1725|01|1\n");
      out.write("00너8027|02|1\n");
      out.write("00더8230|03|1\n");
      out.write("\n");
      out.write("강원80바1004|00\n");
      out.write("강원80바1010|00\n");
      out.write("강원80f바1019|00\n");
      out.write("강원80바1054|00\n");
      out.write("강원80바1081|00\n");
      out.write("강원80바1082|00\n");
      out.write("강원80바1083|00\n");
      out.write("강원80바1086|00\n");
      out.write("강원80바1092|00\n");
      out.write("강원80바1111|00\n");
      out.write("강원80바1119|00\n");
      out.write("강원80바1124|00\n");
      out.write("강원80바1136|00\n");
      out.write("강원80바1153|00\n");
      out.write("\n");
      out.write("</pre>\n");
      out.write("\n");
      out.write("\n");
      out.write("</body>\n");
      out.write("\n");
      out.write("</html>\n");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else log(t.getMessage(), t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
