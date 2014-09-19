package org.yucca.realtime;

/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.FileReader;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.Attribute.Type;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.gml2.GMLReader;

@SiddhiExtension(namespace = "gis", function = "pointInKMLGeometry")
public class PointInKMLGeometryExtension extends FunctionExecutor {

	Logger log = Logger.getLogger(PointInKMLGeometryExtension.class);
	Attribute.Type returnType;
	final GeometryFactory gf = new GeometryFactory();

	/**
	 * Method will be called when initialising the custom function
	 * 
	 * @param types
	 * @param siddhiContext
	 */
	@Override
	public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
		if (types.length!=3) 
		{
			throw new QueryCreationException(
					"PointInKMLGeometryExtension required 3 parameters [x,y,filename.gml]");
			
		}
		returnType = Type.BOOL;
		
		
		for (int i = 0; i < 1; i++) {
			Type attributeType = types[i];
			if (attributeType == Attribute.Type.DOUBLE ||
					attributeType == Attribute.Type.INT ||
					attributeType == Attribute.Type.FLOAT ||
					attributeType == Attribute.Type.LONG) {
			} else  {
				throw new QueryCreationException(
						"PointInKMLGeometryExtension cannot have first two parameters with types not in [double, int, float, long]");
			}
			
			
		}
	}

	/**
	 * Method called when sending events to process
	 * 
	 * @param obj
	 * @return
	 */
	@Override
	protected Object process(Object obj) {
		System.out.println("Process PointInKMLGeometryExtension");
		
		Object[] params = (Object[]) obj;
		Double paramX = Double.parseDouble(String.valueOf(params[0]));
		Double paramY = Double.parseDouble(String.valueOf(params[1]));
		Coordinate coord = new Coordinate(paramX,paramY);
		Point point = gf.createPoint(coord);
		boolean isBound = false;
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			String fileName = String.valueOf(params[2]);			
			FileReader fr = new FileReader(fileName);			
			GMLReader gr = new GMLReader();			
			Geometry g = gr.read(fr, null);
			isBound = point.within(g);
			System.out.println("Process PointInKMLGeometryExtension finished ["+isBound+"]");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isBound;
		
	}

	@Override
	public void destroy() {

	}

	/**
	 * Return type of the custom function mentioned
	 * 
	 * @return
	 */
	@Override
	public Attribute.Type getReturnType() {
		return returnType;
	}

	public static void main(String[] args) {
		try {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		final GeometryFactory gf = new GeometryFactory();
		String fileName = "doc/ztlcentrale.gml";			

		final Coordinate coord = new Coordinate(7.68032,45.07674819);
//		final Coordinate coord = new Coordinate(7.6806123,45.0724792);
		
		final Point point = gf.createPoint(coord);

		
		FileReader fr = new FileReader(fileName);			
		GMLReader gr = new GMLReader();			
		Geometry g = gr.read(fr, null);

		System.out.println(point.within(g));

		
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}

}
