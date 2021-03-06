package kmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LabelStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.SimpleData;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.Units;
import de.micromata.opengis.kml.v_2_2_0.Vec2;

public class KmlUtility {

	// Accepts a KML file and image file(not required) to be part of a KMZ file.
	// IconStyle icon href is changed to the name of the provided image.
	public static File createKmz(File src, File image, String zipName) throws Exception {
		File zip = new File(src.getParent(), zipName);
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
		
		if (image != null) {
			ZipEntry imageFile = new ZipEntry(image.getName());
			out.putNextEntry(imageFile);
			byte[] imageData = Files.readAllBytes(image.toPath());
			out.write(imageData, 0, imageData.length);
			out.closeEntry();
			
			final Kml kml = Kml.unmarshal(src);
			final Document document = (Document) kml.getFeature();
			
			Placemark placemark = (Placemark) document.getFeature().get(0);
			Style styleOne = styleOne(image.getName(), placemark);
			
			document.createAndAddStyle().withId(styleOne.getId()).withIconStyle(styleOne.getIconStyle())
			.withBalloonStyle(styleOne.getBalloonStyle()).withLabelStyle(styleOne.getLabelStyle());
			marshalWithoutPrefix(kml, src, false);
		}

		ZipEntry kmlFile = new ZipEntry(src.getName());
		out.putNextEntry(kmlFile);
		byte[] kmlData = Files.readAllBytes(src.toPath());
		out.write(kmlData, 0, kmlData.length);
		out.closeEntry();

		out.close();
		return zip;
	}
	
	public static List<String> getPlacemarkExtendedDataNames(Kml kml) {
		if (kml == null) return null;
		Document document = (Document) kml.getFeature();
		List<Feature> placemarks = document.getFeature();
		if (placemarks == null || placemarks.size() == 0) return null;
		if (!(placemarks.get(0) instanceof Placemark)) return null;
		Placemark placemark = (Placemark) placemarks.get(0);
		List<Data> data = placemark.getExtendedData().getData();
		List<String> dataNames = new ArrayList<String>();
		for (Data d : data) {
			dataNames.add(d.getName());
		}
		return dataNames;
	}
	
	// Accepts KML object and target file for KML to be written to.
	// Regular marshal that removes all prefixes from KML tags.
	// Minify parameter is used for testing purposes.
	public static void marshalWithoutPrefix(Kml kml, File target, boolean minify) throws Exception {
		Marshaller marshaller = JAXBContext.newInstance(new Class[]{Kml.class}).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		// Removes unwanted prefixes
		marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper()
		{
		    @Override
		    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
		    {
		        return namespaceUri.matches("http://www.w3.org/\\d{4}/Atom") ? "atom"
		                : (
		                namespaceUri.matches("urn:oasis:names:tc:ciq:xsdschema:xAL:.*?") ? "xal"
		                        : (
		                        namespaceUri.matches("http://www.google.com/kml/ext/.*?") ? "gx"
		                                : (
		                                namespaceUri.matches("http://www.opengis.net/kml/.*?") ? ""
		                                        : (
		                                        null
		                                        )
		                                )
		                        )
		                );
		    }
		});
		marshaller.marshal(kml, target);
		if (minify) minify(target);
	}
	
	// Removes line breaks and excess whitespace.
	public static File minify(File src) throws Exception {
		String minifiedName = src.getName().substring(0, src.getName().lastIndexOf(".")) + ".min.kml";
		File minified = new File(src.getParent(), minifiedName);
		
		FileReader fr = new FileReader(src); 
		BufferedReader br = new BufferedReader(fr); 
		FileWriter fw = new FileWriter(minified); 
		String line;

		while((line = br.readLine()) != null) { 
		    line = line.trim(); // remove leading and trailing whitespace
		    if (!line.equals("")) // don't write out blank lines
		    {
		        fw.write(line, 0, line.length());
		    }
		} 
		fr.close();
		fw.close();
		return minified;
	}
	
	// Normalizes KML files for safe processing by this application.
	// All KML files not generated by this application should be cleaned.
	public static Kml cleanKml(Kml dirty) {
		final Document document = (Document) dirty.getFeature();
		List<Feature> placemarks = null;
		
		if (document.getFeature().get(0) instanceof Folder) {
			final Folder folder = (Folder) document.getFeature().get(0);
			placemarks = folder.getFeature();
		}
		if (document.getFeature().get(0) instanceof Placemark) {
			placemarks = document.getFeature();
		}
		
		List<Feature> cleanPlacemarks = new ArrayList<Feature>();
		for (Feature feature : placemarks) {
			Placemark placemark = (Placemark) feature;
			Point point = (Point) placemark.getGeometry();
			
			Placemark temp = KmlFactory.createPlacemark();
			temp.setName(placemark.getName());
			temp.setOpen(true);
			temp.setStyleUrl("#Style1");
			temp.createAndSetPoint().setCoordinates(point.getCoordinates());
			
			ExtendedData tempExtendedData = temp.createAndSetExtendedData();
			List<SimpleData> simpleData = placemark.getExtendedData().getSchemaData().get(0).getSimpleData();
			for (SimpleData data : simpleData) {
				Data tempData = KmlFactory.createData(data.getName());
				tempData.setName(data.getName());
				tempData.setValue(data.getValue());
				tempExtendedData.addToData(tempData);
			}
			cleanPlacemarks.add(temp);
		}
		
		final Kml clean = new Kml();
		Style styleOne = styleOne(null, (Placemark)cleanPlacemarks.get(0));
		clean.createAndSetDocument().withOpen(true).withFeature(cleanPlacemarks).createAndAddStyle().withId(styleOne.getId())
		.withIconStyle(styleOne.getIconStyle()).withBalloonStyle(styleOne.getBalloonStyle()).withLabelStyle(styleOne.getLabelStyle());
		return clean;
	}
	
	// Dynamically styles KML based on Balloon Icon and Placemark fields
	public static Style styleOne(String href, Placemark placemark) {
		List<String> fieldNames = new ArrayList<String>();
		if (placemark != null && placemark.getExtendedData() != null) {
			for (Data data : placemark.getExtendedData().getData()) {
				fieldNames.add(data.getName());
			}
		}
		
		Style style = KmlFactory.createStyle();
		style.setId("Style1");
		
		if (href != null) {
			Icon icon = KmlFactory.createIcon().withHref(href);
			Vec2 hotspot = KmlFactory.createVec2().withX(0.5).withY(0.5).withXunits(Units.FRACTION).withYunits(Units.FRACTION);
			IconStyle iconStyle = KmlFactory.createIconStyle().withScale(1.0).withIcon(icon).withHotSpot(hotspot);
			style.setIconStyle(iconStyle);
		}
		
		LabelStyle labelStyle = KmlFactory.createLabelStyle().withScale(0.4);
		style.setLabelStyle(labelStyle);
		/*StringBuilder balloonBuilder = new StringBuilder("<![CDATA[<table border=\"0\"><br/>");
		for (String fieldName : fieldNames) {
			balloonBuilder.append("<tr><td><b>" + fieldName + ": </b></td><td>$[" + fieldName + "]</td></tr><br/>");
		}
		balloonBuilder.append("</table>");
		BalloonStyle balloonStyle = KmlFactory.createBalloonStyle().withText(balloonBuilder.toString());
		style.setBalloonStyle(balloonStyle);*/
		return style;
	}
	
}
