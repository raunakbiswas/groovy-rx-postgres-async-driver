package com.github.leosilvadev.groovypgasync

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

import rx.functions.Func1

import com.github.leosilvadev.groovypgasync.exceptions.ResultMapException
import com.github.pgasync.ResultSet
import com.github.pgasync.Row

@CompileStatic
@TypeChecked
class PgDbResultMapper {
	
	static Func1 mapMany(Map<String, Class> objectTemplate){
		{ ResultSet result ->
			def items = []
			result.iterator().each { row ->
				def map = [:]
				result.columns.each { column ->
					column = column.toLowerCase()
					if ( objectTemplate.keySet().contains(column) ) {
						def type = objectTemplate.get(column)
						map.put(column, mapType(column, type, row))
					}
				}
				items << map
			}
			items
		} as Func1
	}
	
	static Func1 mapOne(Map<String, Class> objectTemplate){
		{ ResultSet result ->
			if ( result.size() > 1 ) throw new ResultMapException("Expected only one result but got many. [${result.size()}]")
			
			def row = result.iterator().next()
			def map = [:]
			result.columns.each { column ->
				column = column.toLowerCase()
				if ( objectTemplate.keySet().contains(column) ) {
					def type = objectTemplate.get(column)
					map.put(column, mapType(column, type, row))
					
				}
			}
			map
		} as Func1
	}
	
	static def mapType(String column, Class type, Row row){
		switch(type) {
			case BigDecimal: return row.getBigDecimal(column)
			case BigInteger: return row.getBigInteger(column)
			case Boolean: return row.getBoolean(column)
			case Byte: return row.getByte(column)
			case Character: return row.getChar(column)
			case Calendar: return calendarFrom(column, row)
			case Time: return row.getTime(column)
			case Timestamp: return row.getTimestamp(column)
			case Date: return dateFrom(column, row)
			case Double: return row.getDouble(column)
			case Integer: return row.getInt(column)
			case LocalDate: return localDateFrom(column, row)
			case LocalDateTime: return localDateTimeFrom(column, row)
			case Long: return row.getLong(column)
			case Short: return row.getShort(column)	
			case String: return row.getString(column)
			case Map: return row.get(column, Map)
			default: throw new IllegalArgumentException('Unknown type')
		}
	}
	
	static Date dateFrom(String column, Row row) {
		java.sql.Date date = row.getDate(column)
		new Date(date.time)
	}
	
	static Calendar calendarFrom(String column, Row row) {
		java.sql.Date date = row.getDate(column)
		date.toCalendar()
	}
	
	static LocalDate localDateFrom(String column, Row row) {
		java.sql.Date date = row.getDate(column)
		date.toLocalDate()
	}
	
	static LocalDateTime localDateTimeFrom(String column, Row row) {
		java.sql.Timestamp date = row.getTimestamp(column)
		date.toLocalDateTime()
	}

}
