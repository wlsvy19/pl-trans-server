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
      out.write("<td> ?????? Type </td><td><input type=\"text\" name=\"callType\" value=\"");
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
      out.write("<td> ???????????? </td><td><input type=\"text\" name=\"carno\" value=\"");
      out.print( carNo );
      out.write("\" /> <br/>\n");
      out.write("  <bold>");
      out.print( UtilExt.print( carBcdNo ) );
      out.write(" </bold>\n");
      out.write("</td>\n");
      out.write("</tr>\n");
      out.write("<tr>\n");
      out.write("<td colspan=\"2\"> <input type=\"submit\" name=\"data\" value=\"??????\"/> </td>\n");
      out.write("</tr>\n");
      out.write("\n");
      out.write("<tr>\n");
      out.write("<td> ?????????????????? </td><td> ");
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
      out.write("<td> PL search </td><td> ** ?????????????????? ?????? ?????? ????????? - ???????????????????????? ?????? ( 1 ~ 6 ) ??? ?????????, PL ??? ??????  ");
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
      out.write("?????? : ????????????(12)|????????????(5)|????????????(5)|??????(2)|????????????(1)\n");
      out.write("         ??? : ??????12???1234|21000|3|00|1\n");
      out.write("               34???5678|9000|2|00|1\n");
      out.write("\n");
      out.write("??????(2)\n");
      out.write("00 : ??????(BL)\n");
      out.write("01 : ????????????(RL)\n");
      out.write("02 : ??????&????????????\n");
      out.write("03 : ??????\n");
      out.write("????????????(1)\n");
      out.write("1 : ??????\n");
      out.write("2 : ??????\n");
      out.write("3 : ??????\n");
      out.write("\n");
      out.write("00???2874|500|1|00|1\n");
      out.write("00???1725|100|2|01|1\n");
      out.write("00???8027|100|3|02|1\n");
      out.write("00???8230|200|4|03|1\n");
      out.write("01???0433|4|2|00|1\n");
      out.write("01???0434|5|1|00|1\n");
      out.write("01???0463|6|1|00|1\n");
      out.write("\n");
      out.write("** ???????????? OLD ???????????? TEST ...\n");
      out.write("\n");
      out.write("??????8???1234|500|1|00|1\n");
      out.write("??????8???1235|100|2|01|1\n");
      out.write("??????8???1236|100|3|02|1\n");
      out.write("??????8???1237|200|4|03|1\n");
      out.write("\n");
      out.write("\n");
      out.write("01???0115|450|1|00|1\n");
      out.write("01???0122|4700|10|00|1\n");
      out.write("01???0124|55900|7|00|1\n");
      out.write("01???0195|800|1|00|1\n");
      out.write("\n");
      out.write("</pre>\n");
      out.write("\n");
      out.write("<hr>\n");
      out.write("\n");
      out.write("PL\n");
      out.write("\n");
      out.write("<pre>\n");
      out.write("\n");
      out.write("????????????(12)|??????(2)|????????????(1)\n");
      out.write("         ??? : ??????12???1234|00|1\n");
      out.write("\n");
      out.write("??????(2)\n");
      out.write("\n");
      out.write("00 : ??????(PL)\n");
      out.write("????????????(1)\n");
      out.write("1 : ??????\n");
      out.write("2 : ??????\n");
      out.write("3 : ??????\n");
      out.write("\n");
      out.write("\n");
      out.write("00???2874|00|1\n");
      out.write("00???1725|01|1\n");
      out.write("00???8027|02|1\n");
      out.write("00???8230|03|1\n");
      out.write("\n");
      out.write("\n");
      out.write("01???0115|00|1\n");
      out.write("01???0122|00|1\n");
      out.write("01???0131|00|1\n");
      out.write("01???0147|00|1\n");
      out.write("01???0155|00|1\n");
      out.write("01???0180|00|1\n");
      out.write("01???0187|00|1\n");
      out.write("01???0203|00|1\n");
      out.write("01???0204|00|1\n");
      out.write("01???0210|00|1\n");
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
      out.write("????????????(12)|??????(2)|????????????(1)\n");
      out.write("         ??? : ??????12???1234|00|1\n");
      out.write("\n");
      out.write("??????(2)\n");
      out.write("00 : ????????????(STPL)\n");
      out.write("????????????(1)\n");
      out.write("1 : ??????\n");
      out.write("2 : ??????\n");
      out.write("3 : ??????\n");
      out.write("\n");
      out.write("00???2874|00|1\n");
      out.write("00???1725|01|1\n");
      out.write("00???8027|02|1\n");
      out.write("00???8230|03|1\n");
      out.write("\n");
      out.write("??????80???1004|00\n");
      out.write("??????80???1010|00\n");
      out.write("??????80f???1019|00\n");
      out.write("??????80???1054|00\n");
      out.write("??????80???1081|00\n");
      out.write("??????80???1082|00\n");
      out.write("??????80???1083|00\n");
      out.write("??????80???1086|00\n");
      out.write("??????80???1092|00\n");
      out.write("??????80???1111|00\n");
      out.write("??????80???1119|00\n");
      out.write("??????80???1124|00\n");
      out.write("??????80???1136|00\n");
      out.write("??????80???1153|00\n");
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
