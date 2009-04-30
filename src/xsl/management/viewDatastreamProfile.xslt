<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="fedora"/>
	<xsl:output method="html" indent="yes"/>
	<xsl:template match="datastreamProfile">
		<html>
			<head>
				<title>Datastream Profile HTML Presentation</title> 
			</head>
			<body>
				<center>
					<table width="784" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td width="141" height="134" valign="top">
								<img src="/{$fedora}/images/newlogo2.jpg" width="141" height="134"/>
							</td>
							<td width="643" valign="top">
								<center>
									<h2>Fedora Digital Object Datastream</h2>
									<h3>Datastream Profile View</h3>	
								</center>
							</td>
						</tr>
					</table>
					<hr/>
					<xsl:choose>
						<xsl:when test="@dateTime">
							<font size="+1" color="blue">Version Date:   </font>
							<font size="+1"><xsl:value-of select="@dateTime"/></font>
						</xsl:when>
						<xsl:otherwise>
							<font size="+1" color="blue">Version Date:   </font>
							<font size="+1">current</font>	
						</xsl:otherwise>
					</xsl:choose>
					<p/>					
					<hr/>
					<table width="784" border="1" cellpadding="5" cellspacing="5" bgcolor="silver">
					<tr>
						<td align="right">
							<font color="blue">Object Identifier (PID): </font>
						</td>
						<td align="left">
							<xsl:value-of select="@pid"/>
						</td>
					</tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Identifier (DSID): </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="@dsID"/>
                        </td>
                    </tr>
					<tr>
						<td align="right">
							<font color="blue">Datastream Label: </font>
						</td>
						<td align="left">
							<xsl:value-of select="dsLabel"/>
						</td>
					</tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Version ID: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsVersionID"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Creation Date: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsCreateDate"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream State: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsState"/>
                        </td>
                    </tr>					
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream MIME type: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsMIME"/>
                        </td>
                    </tr>					
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Format URI: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsFormatURI"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Control Group: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsControlGroup"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Size: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsSize"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Versionable: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsVersionable"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Info Type: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsInfoType"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Location: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsLocation"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Location Type: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsLocationType"/>
                        </td>
                    </tr>                    
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Checksum Type: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsChecksumType"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">
                            <font color="blue">Datastream Checksum: </font>
                        </td>
                        <td align="left">
                            <xsl:value-of select="dsChecksum"/>
                        </td>
                    </tr>				
					<xsl:for-each select="dsAltID">
                        <tr>
                            <td align="right">
                                <font color="blue">Datastream Alternate ID: </font>
                            </td>
                            <td align="left">
                                <xsl:value-of select="."/>
                            </td>
                        </tr>
					</xsl:for-each>							
					</table>
				</center>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>