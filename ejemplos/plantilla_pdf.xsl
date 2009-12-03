<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:tedbarcode="cl.nic.dte.fop.TedBarcodeExtension"
	extension-element-prefixes="tedbarcode"
	>

	<xsl:output method="xml" version="1.0" omit-xml-declaration="no"
		indent="yes" />

	<xsl:param name="versionParam" select="'1.0'" />
	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="simple"
					page-height="27.9cm" page-width="21.6cm" margin-top="1cm"
					margin-bottom="2cm" margin-left="1cm" margin-right="1cm">
					<fo:region-body margin-top="0cm" />
					<fo:region-before extent="3cm" />
					<fo:region-after extent="1.5cm" />
				</fo:simple-page-master>
			</fo:layout-master-set>


			<fo:page-sequence master-reference="simple">
				<fo:flow flow-name="xsl-region-body">
					<xsl:apply-templates select="DTE/Documento" />
				</fo:flow>
			</fo:page-sequence>

		</fo:root>
	</xsl:template>


	<xsl:template match="DTE/Documento">
		<fo:block>
			<xsl:apply-templates select="Encabezado/Emisor">
				<xsl:with-param name="folio">
					<xsl:value-of select="Encabezado/IdDoc/Folio" />
				</xsl:with-param>
				<xsl:with-param name="tipo">
					<xsl:value-of select="Encabezado/IdDoc/TipoDTE" />
				</xsl:with-param>
			</xsl:apply-templates>
			<xsl:apply-templates select="Encabezado/Receptor">
				<xsl:with-param name="fecha">
					<xsl:value-of select="Encabezado/IdDoc/FchEmis" />
				</xsl:with-param>
				<xsl:with-param name="medioPago">
					<xsl:value-of select="Encabezado/IdDoc/MedioPago" />
				</xsl:with-param>
				<xsl:with-param name="formaPago">
					<xsl:value-of select="Encabezado/IdDoc/FmaPago" />
				</xsl:with-param>
			</xsl:apply-templates>

			<!--  La lista de detalle -->
			<fo:block-container absolute-position="absolute" left="0cm"
				top="8cm">
				<fo:block font-size="8pt" font-family="monospace"
					color="black" text-align="left" space-before="8pt">
					<fo:table table-layout="fixed" width="100%"
						border-collapse="collapse">
						<fo:table-column column-width="2cm" />
						<fo:table-column column-width="12.5cm" />
						<fo:table-column column-width="2.5cm" />
						<fo:table-column column-width="2.5cm" />

						<fo:table-body>
							<fo:table-row>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid">
									<fo:block>
										<fo:inline font-weight="bold">
											Cantidad
										</fo:inline>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid">
									<fo:block>
										<fo:inline font-weight="bold">
											Detalle
										</fo:inline>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid">
									<fo:block>
										<fo:inline font-weight="bold">
											P. Unitario
										</fo:inline>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid">
									<fo:block>
										<fo:inline font-weight="bold">
											Total
										</fo:inline>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<xsl:choose>
								<xsl:when test="Detalle[NroLinDet=1]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=1]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="Detalle[NroLinDet=2]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=2]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="Detalle[NroLinDet=3]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=3]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="Detalle[NroLinDet=4]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=4]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="Detalle[NroLinDet=5]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=5]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="Detalle[NroLinDet=6]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=6]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="Detalle[NroLinDet=7]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=7]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="Detalle[NroLinDet=8]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=8]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when test="Detalle[NroLinDet=9]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=9]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when
									test="Detalle[NroLinDet=10]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=10]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when
									test="Detalle[NroLinDet=11]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=11]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when
									test="Detalle[NroLinDet=12]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=12]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when
									test="Detalle[NroLinDet=13]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=13]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when
									test="Detalle[NroLinDet=14]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=14]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:choose>
								<xsl:when
									test="Detalle[NroLinDet=15]">
									<xsl:apply-templates
										select="Detalle[NroLinDet=15]" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template
										name="DetalleVacio" />
								</xsl:otherwise>
							</xsl:choose>

	
							<fo:table-row>
								<fo:table-cell text-align="center"
									border-left-width="0.5pt" border-left-style="solid"
									border-right-width="0.5pt" border-right-style="solid"
									border-bottom-width="0.5pt" border-bottom-style="solid"
									number-columns-spanned="4">
									<fo:block />
								</fo:table-cell>
							</fo:table-row>
							<fo:table-row>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid" display-align="center" column-number="3" height="1cm">
									<fo:block>
										<fo:inline font-weight="bold">
											Suma
										</fo:inline>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid" column-number="4" display-align="center" height="1cm">
									<fo:block>
										<fo:inline font-weight="bold">
											<xsl:value-of select="Encabezado/Totales/MntNeto"/>
										</fo:inline>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<fo:table-row>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid" column-number="3" display-align="center" height="1cm">
									<fo:block>
										<fo:inline font-weight="bold">
											IVA <xsl:value-of select="Encabezado/Totales/TasaIVA"/>%
										</fo:inline>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid" column-number="4" display-align="center" height="1cm">
									<fo:block>
										<fo:inline font-weight="bold">
											<xsl:value-of select="Encabezado/Totales/IVA"/>
										</fo:inline>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<fo:table-row>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid" column-number="3" display-align="center" height="1cm">
									<fo:block>
										<fo:inline font-weight="bold">
											Total
										</fo:inline>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell text-align="center"
									border-width="0.5pt" border-style="solid" column-number="4" display-align="center" height="1cm">
									<fo:block>
										<fo:inline font-weight="bold">
											<xsl:value-of select="Encabezado/Totales/MntTotal"/>
										</fo:inline>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
						</fo:table-body>
					</fo:table>
				</fo:block>
			</fo:block-container>
			<xsl:apply-templates select="TED" />
		</fo:block>
	</xsl:template>


	<!-- Datos del emisor -->
	<xsl:template match="Emisor">
		<xsl:param name="folio" />
		<xsl:param name="tipo" />

		<!--  El logo -->
		<fo:block-container absolute-position="absolute" left="0cm"
			top="0cm">
			<fo:block>
				<fo:external-graphic
					src="url('logo.gif')"/>
			</fo:block>
		</fo:block-container>

		<fo:block-container absolute-position="absolute" left="2.5cm"
			top="0cm" width="9cm">

			<fo:block font-size="18pt" font-family="Helvetica"
				font-weight="bold" text-align="left" color="blue">
				<xsl:value-of select="RznSoc" />
			</fo:block>

			<xsl:if test="Sucursal">
				<fo:block font-weight="bold" font-size="12pt" font-family="monospace"
					language="es" hyphenate="true" color="black" text-align="left">
					Sucursal: <xsl:value-of select="Sucursal" /> (Codigo SII: <xsl:value-of select="CdgSIISucur" />)
				</fo:block>
			</xsl:if>

			<fo:block font-weight="bold" font-size="12pt" font-family="monospace"
				language="es" hyphenate="true" color="black" text-align="left">
				<xsl:value-of select="GiroEmis" />
			</fo:block>

			<fo:block font-weight="bold" font-size="12pt" font-family="monospace"
				language="es" hyphenate="true" color="black" text-align="left">
				<xsl:value-of select="DirOrigen" />
			</fo:block>

			<fo:block font-weight="bold" font-size="12pt" font-family="monospace"
				language="es" hyphenate="true" color="black" text-align="left">
				<xsl:value-of select="CmnaOrigen" />
				,
				<xsl:value-of select="CiudadOrigen" />
			</fo:block>

		</fo:block-container>

		<!-- Recuadro con folio -->
		<fo:block-container absolute-position="absolute" top="0cm"
			margin-top="0.5cm" left="12cm" height="3cm" width="7.5cm"
			border-color="green" border-style="solid" border-width="0.5mm">
			<fo:block font-size="14pt" font-family="monospace"
				font-weight="bold" color="green" text-align="center"
				hyphenate="false">
				R.U.T.:
				<xsl:call-template name="RutFormat">
					<xsl:with-param name="rut">
						<xsl:value-of select="RUTEmisor" />
					</xsl:with-param>
				</xsl:call-template>
			</fo:block>
			<fo:block font-size="14pt" font-family="monospace"
				font-weight="bold" color="green" text-align="center">
				<xsl:choose>
					<xsl:when test="$tipo=33">
						FACTURA ELECTRONICA
					</xsl:when>
					<xsl:when test="$tipo=52">
						GUIA DE DESPACHO ELECTRONICA
					</xsl:when>
					<xsl:when test="$tipo=56">
						NOTA DE DEBITO ELECTRONICA
					</xsl:when>
					<xsl:when test="$tipo=61">
						NOTA DE CREDITO ELECTRONICA
					</xsl:when>
					<xsl:otherwise>
						CORREGIR EN TEMPLATE XSL
					</xsl:otherwise>
				</xsl:choose>
			</fo:block>
			<fo:block font-size="14pt" font-family="monospace"
				font-weight="bold" color="green" text-align="center">
				N&#176;
				<xsl:value-of select="$folio" />
			</fo:block>
		</fo:block-container>

	</xsl:template>

	<!-- Datos del receptor -->
	<xsl:template match="Receptor">
		<xsl:param name="fecha" />
		<xsl:param name="medioPago"/>
		<xsl:param name="formaPago"/>

     	<fo:block-container absolute-position="absolute" left="0cm"
			top="4cm">
			<fo:block font-size="10pt" font-family="monospace" space-after="8pt"
				language="es" hyphenate="true" color="black" text-align="left">
				Santiago,
				<xsl:call-template name="FechaFormat">
					<xsl:with-param name="fecha">
						<xsl:value-of select="$fecha" />
					</xsl:with-param>
				</xsl:call-template>

			</fo:block>
		
			<fo:block font-size="10pt" font-family="monospace"
				language="es" hyphenate="true" color="black" text-align="left">
				<fo:table table-layout="fixed" width="100%">
					<fo:table-column column-width="3cm" />
					<fo:table-column column-width="5cm" />
					<fo:table-column column-width="4cm" />
					<fo:table-column column-width="5cm" />


					<fo:table-body>
						<fo:table-row>
							<fo:table-cell text-align="left">
								<fo:block>
									<fo:inline font-weight="bold">
										SE&#209;OR(ES):
									</fo:inline>
								</fo:block>
							</fo:table-cell>
							<fo:table-cell text-align="left"
								number-columns-spanned="3">
								<fo:block>
									<xsl:value-of select="RznSocRecep" />
								</fo:block>
							</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
							<fo:table-cell text-align="left">
								<fo:block>
									<fo:inline font-weight="bold">
										R.U.T.:
									</fo:inline>
								</fo:block>
							</fo:table-cell>
							<fo:table-cell text-align="left"
								number-columns-spanned="3">
								<fo:block>
									<xsl:call-template
										name="RutFormat">
										<xsl:with-param name="rut">
											<xsl:value-of
												select="RUTRecep" />
										</xsl:with-param>
									</xsl:call-template>
								</fo:block>
							</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
							<fo:table-cell text-align="left">
								<fo:block>
									<fo:inline font-weight="bold">
										DIRECCION:
									</fo:inline>
								</fo:block>
							</fo:table-cell>
							<fo:table-cell text-align="left"
								number-columns-spanned="3">
								<fo:block>
									<xsl:value-of select="DirRecep" />
								</fo:block>
							</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
							<fo:table-cell text-align="left">
								<fo:block>
									<fo:inline font-weight="bold">
										COMUNA:
									</fo:inline>
								</fo:block>
							</fo:table-cell>
							<fo:table-cell text-align="left">
								<fo:block>
									<xsl:value-of select="CmnaRecep" />
								</fo:block>
							</fo:table-cell>
							<fo:table-cell text-align="left">
								<fo:block>
									<fo:inline font-weight="bold">
										CIUDAD:
									</fo:inline>
								</fo:block>
							</fo:table-cell>
							<fo:table-cell text-align="left">
								<fo:block>
									<xsl:value-of select="CiudadRecep" />
								</fo:block>
							</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
							<fo:table-cell text-align="left">
								<fo:block>
									<fo:inline font-weight="bold">
										GIRO:
									</fo:inline>
								</fo:block>
							</fo:table-cell>
							<fo:table-cell text-align="left">
								<fo:block>
									<xsl:value-of select="GiroRecep" />
								</fo:block>
							</fo:table-cell>
							<fo:table-cell text-align="left">
								<fo:block>
									<fo:inline font-weight="bold">
										CONDICION VENTA:
									</fo:inline>
								</fo:block>
							</fo:table-cell>
							<fo:table-cell text-align="left" height="1cm">
								<fo:block>
									<xsl:call-template
										name="PagoFormat">
										<xsl:with-param name="medioPago">
											<xsl:value-of
												select="$medioPago" />
										</xsl:with-param>
										<xsl:with-param name="formaPago">
											<xsl:value-of
												select="$formaPago" />
										</xsl:with-param>
									</xsl:call-template>
								</fo:block>
							</fo:table-cell>
						</fo:table-row>
					</fo:table-body>
				</fo:table>

			</fo:block>
</fo:block-container>
	</xsl:template>

<!-- Detalle -->
	<xsl:template match="Detalle">
		<fo:table-row >
			<fo:table-cell text-align="right" border-left-width="0.5pt"
				border-left-style="solid" border-right-width="0.5pt"
				border-right-style="solid" margin-right="2mm"  height="0.8cm">
				<fo:block>
						<xsl:value-of select="QtyItem" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell text-align="left" border-left-width="0.5pt"
				border-left-style="solid" border-right-width="0.5pt"
				border-right-style="solid"  margin-right="2mm" margin-left="2mm"  height="0.8cm">
				<fo:block >
						<xsl:value-of select="NmbItem" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell text-align="right" border-left-width="0.5pt"
				border-left-style="solid" border-right-width="0.5pt"
				border-right-style="solid" margin-right="2mm"  height="0.8cm">
				<fo:block>
						<xsl:value-of select="PrcItem" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell text-align="right" border-left-width="0.5pt"
				border-left-style="solid" border-right-width="0.5pt"
				border-right-style="solid" margin-right="2mm" height="0.8cm" >
				<fo:block>
						<xsl:value-of select="MontoItem"/>
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>

	<!-- Timbre electrónico -->
	<xsl:template match="TED">
		<xsl:variable name="myted" select="." />
		<xsl:variable name="barcode-cfg">
			<barcode>
				<!--  Segun SII, 3cm x 9cm max -->
				<pdf417>
					<module-width>0.008in</module-width>
					<!--  min exigido por Sii 0.0067  -->
					<row-height>3mw</row-height>
					<!--  3 veces el ancho -->
					<quite-zone enabled="true">0.25in</quite-zone>
					<ec-level>5</ec-level>
					<columns>14</columns>
				</pdf417>
			</barcode>
		</xsl:variable>
		<fo:block-container absolute-position="absolute" top="21cm"
			width="7cm">
			<fo:block>
				<fo:instream-foreign-object>
			
					<xsl:copy-of
						select="tedbarcode:generate($barcode-cfg, $myted)" />
			
				</fo:instream-foreign-object>
			</fo:block>
			<fo:block font-size="8pt" font-family="sans-serif"
				text-align="center">
				Timbre Electrónico SII
			</fo:block>
			<fo:block font-size="8pt" font-family="sans-serif"
				text-align="center">
				Res. XX de 2007 - Verifique Documento: www.sii.cl
			</fo:block>
		</fo:block-container>
	</xsl:template>

	<xsl:template name="PagoFormat">
		<xsl:param name="medioPago" />
		<xsl:param name="formaPago" />

		<xsl:choose>
			<xsl:when test="$medioPago='CH'">Cheque</xsl:when>
			<xsl:when test="$medioPago='LT'">Letra</xsl:when>
			<xsl:when test="$medioPago='EF'">Efectivo</xsl:when>
			<xsl:when test="$medioPago='PE'">Pago a Cta. Corriente</xsl:when>
			<xsl:when test="$medioPago='TC'">Tarjeta de Crédito</xsl:when>
			<xsl:when test="$medioPago='CF'">Cheque a Fecha</xsl:when>
			<xsl:when test="$medioPago='OT'">Otro</xsl:when>
		</xsl:choose>

		<xsl:choose>
			<xsl:when test="$formaPago=1"> (Contado)</xsl:when>
			<xsl:when test="$formaPago=2"> (Crédito)</xsl:when>
			<xsl:when test="$formaPago=3"> (Sin Valor)</xsl:when>
		</xsl:choose>

	</xsl:template>

	<xsl:template name="FechaFormat">
		<xsl:param name="fecha" />

		<xsl:value-of
			select="substring($fecha,string-length($fecha)-1,2)" />
		de
		<xsl:choose>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=01">
				Enero
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=02">
				Febrero
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=03">
				Marzo
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=04">
				Abril
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=05">
				Mayo
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=06">
				Junio
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=07">
				Julio
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=08">
				Agosto
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=09">
				Septiembre
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=10">
				Octubre
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=11">
				Noviembre
			</xsl:when>
			<xsl:when
				test="substring($fecha,string-length($fecha)-4,2)=12">
				Diciembre
			</xsl:when>
		</xsl:choose>
		de
		<xsl:value-of
			select="substring($fecha,string-length($fecha)-9,4)" />
	</xsl:template>

	<xsl:template name="RutFormat">
		<xsl:param name="rut" />
		<xsl:variable name="num" select="substring-before($rut,'-')" />
		<xsl:variable name="dv" select="substring-after($rut,'-')" />

		<xsl:value-of select="substring($num,string-length($num)-8,3)" />.<xsl:value-of
		 select="substring($num,string-length($num)-5,3)" />.<xsl:value-of 
		 select="substring($num,string-length($num)-2,3)" />-<xsl:value-of select="$dv" />

	</xsl:template>

	<xsl:template name="DetalleVacio">
		<fo:table-row>
			<fo:table-cell text-align="center" border-left-width="0.5pt"
				border-left-style="solid" border-right-width="0.5pt"
				border-right-style="solid" height="0.8cm">
				<fo:block white-space-treatment="preserve">&#xa0;</fo:block>
			</fo:table-cell>
			<fo:table-cell text-align="center" border-left-width="0.5pt"
				border-left-style="solid" border-right-width="0.5pt"
				border-right-style="solid" height="0.8cm">
				<fo:block white-space-treatment="preserve">&#xa0;</fo:block>
			</fo:table-cell>
			<fo:table-cell text-align="center" border-left-width="0.5pt"
				border-left-style="solid" border-right-width="0.5pt"
				border-right-style="solid" height="0.8cm">
				<fo:block white-space-treatment="preserve">&#xa0;</fo:block>
			</fo:table-cell>
			<fo:table-cell text-align="center" border-left-width="0.5pt"
				border-left-style="solid" border-right-width="0.5pt"
				border-right-style="solid" height="0.8cm">
				<fo:block white-space-treatment="preserve">&#xa0;</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>

</xsl:stylesheet>


