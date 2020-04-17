<%@ include file="../includes/header.jsp" %>

<div class="animated bounceInDown" style="font-size:48pt; font-family:arial; color:#990000; font-weight:bold">Web
    Opinion Visualiser
</div>

</p>&nbsp;</p>&nbsp;</p>

<table width="600" cellspacing="0" cellpadding="7" border="0">
    <tr>
        <td valign="top">

            <form bgcolor="white" method="POST" action="doProcess">
                <fieldset>
                    <legend><h3>Specify Details</h3></legend>
                    <p>
                        Please be patient after submitting as it may take some time to parser web pages for data

                        <b>Word Cloud Size:</b>
                    <p>
                        <select name="sizeOptions">
                            <option> 32</option>
                            <option selected>64</option>
                            <option>128</option>
                            <option>256</option>
                        </select>


                    <p>
                        <b>DuckDuckGo is the search engine</b>

                    <p> Jsoup is a good keyword to test</p>

                    <p/>

                    <b>Please enter a keyword you wish to search:</b><br>
                    <input name="query" size="100">
                    <p/>


                    <center><input type="submit" value="Search & Visualise!"></center>
                </fieldset>
            </form>

        </td>
    </tr>
</table>
<%@ include file="../includes/footer.jsp" %>

