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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.Attribute.Type;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

@SiddhiExtension(namespace = "gis", function = "pointInLinearRing")
public class PointInLinearRingExtension extends FunctionExecutor {

	Logger log = Logger.getLogger(PointInLinearRingExtension.class);
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
		if (types.length<8) // veeificare che il numero sia pari
		{
			throw new QueryCreationException(
					"pointInLinearRing required more then 7 parameters [x,y,px1,py1,px2,py2,px3,py3,...]");
			
		}
		
		for (Attribute.Type attributeType : types) {
			if (attributeType == Attribute.Type.DOUBLE ||
					attributeType == Attribute.Type.INT ||
					attributeType == Attribute.Type.FLOAT ||
					attributeType == Attribute.Type.LONG) {
				returnType = Type.BOOL;
			} else  {
				throw new QueryCreationException(
						"pointInLinearRing cannot have parameters with types not in [double, int, float, long]");
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
		System.out.println("Process PointInLinearRingExtension");
		
		Object[] params = (Object[]) obj;
		Double paramX = Double.parseDouble(String.valueOf(params[0]));
		Double paramY = Double.parseDouble(String.valueOf(params[1]));
		Coordinate coord = new Coordinate(paramX,paramY);
		Point point = gf.createPoint(coord);
		
		ArrayList<Coordinate> pointsOfLinearRing = new ArrayList<Coordinate>();
		
		for (int i = 2; i < params.length; i=i+2) {
			paramX = Double.parseDouble(String.valueOf(params[i]));
			paramY = Double.parseDouble(String.valueOf(params[i+1]));
			pointsOfLinearRing.add(new Coordinate(paramX,paramY));
		}
		Polygon polygon = gf.createPolygon(
				new LinearRing(new CoordinateArraySequence(pointsOfLinearRing
						.toArray(new Coordinate[pointsOfLinearRing.size()])), gf), null);
		
		boolean isBound = point.within(polygon);
		System.out.println("Process PointInLinearRingExtension finished ["+isBound+"]");
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
		final GeometryFactory gf = new GeometryFactory();

		final ArrayList<Coordinate> points = new ArrayList<Coordinate>();
		points.add(new Coordinate(-10, -10));
		points.add(new Coordinate(-10, 10));
		points.add(new Coordinate(10, 10));
		points.add(new Coordinate(10, -10));
		points.add(new Coordinate(-10, -10));
		final Polygon polygon = gf.createPolygon(
				new LinearRing(new CoordinateArraySequence(points
						.toArray(new Coordinate[points.size()])), gf), null);

		final Coordinate coord = new Coordinate(0, 0);
		final Point point = gf.createPoint(coord);

		System.out.println(point.within(polygon));
	}

}
