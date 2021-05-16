package com.fudandori.xpenser.v2.process;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.fudandori.xpenser.v2.Ctx;



public class Processor {

	private int balanceColumn;
	private int conceptColumn;
	private int expensesColumn;
	private int dateColumn;
	private int startRow;
	
	private File file;
	private String lastBalance;
	
	public Processor(File file, int balance, int concept, int expenses, int date, int startRow) {
		this.file = file;
		this.balanceColumn = balance;
		this.conceptColumn = concept;
		this.expensesColumn = expenses;
		this.dateColumn = date;
		this.startRow = startRow;
	}

	public Map<String, Float> process() throws IOException {

		Map<String, Float> result = new HashMap<>();

		Iterator<Row> i = getIterator(file);

		while (i.hasNext()) {
			Row row = i.next();
			map(row, result);
		}

		return result;
	}

	public Map<String, Map<String, Float>> processMonthly() throws IOException {

		Map<String, Map<String, Float>> result = new HashMap<>();

		Iterator<Row> i = getIterator(file);

		while (i.hasNext()) {
			Row row = i.next();
			map(row, getMap(row, result));
		}

		return sortByKey(result);
	}

	private Map<String, Float> getMap(Row row, Map<String, Map<String, Float>> source) {
		Map<String, Float> result = null;

		if (row.getRowNum() >= startRow - 1) {

			String cellValue = getCellValue(row, dateColumn);

			if (cellValue != null) {

				final String date = cellValue.substring(0, 7) + "-01";

				source.putIfAbsent(date, new HashMap<>());
				result = source.get(date);
			}
		}

		return result;
	}

	private Map<String, Map<String, Float>> sortByKey(Map<String, Map<String, Float>> input) {
		return input.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(
							Entry::getKey,
							Entry::getValue,
							(e1, e2) -> e2,
							LinkedHashMap<String, Map<String, Float>>::new
						));
	}
	
	private static float round(float d) {
		return new BigDecimal(Float.toString(d)).setScale(2, RoundingMode.HALF_UP).floatValue();
	}

	private static String getCellValue(Row row, int column) {
		String result = null;
		Cell cell = row.getCell(column);
		CellType type = cell.getCellType();

		if (type == CellType.STRING) {
			
			result = cell.getStringCellValue();
			
		} else if (type == CellType.NUMERIC) {
			
			result = DateUtil.isCellDateFormatted(cell)
					? DateUtil
							.getJavaDate(cell.getNumericCellValue())
							.toInstant()
							.atZone(ZoneId.systemDefault())
							.toLocalDate()
							.toString()
					: Double.toString(cell.getNumericCellValue());
		}

		return result;
	}

	private Iterator<Row> getIterator(File file) throws IOException {
		Iterator<Row> it = null;
		
		try (Workbook wb = WorkbookFactory.create(file)) {
			it = wb.getSheetAt(0).rowIterator();
		}
		
		return it;
	}
	
	private void map(Row row, Map<String, Float> map) {

		boolean inRange = row.getRowNum() >= startRow - 1;

		if (inRange) {

			String balance = getCellValue(row, balanceColumn);
			
			if(balance == null) {
				balance = "";
			}
			
			boolean duplicate = balance.equals(lastBalance);

			if (!duplicate) {

				lastBalance = balance;

				String concept = String.valueOf(getCellValue(row, conceptColumn)).replace("PAGO EN EL DIA TJ-", "");
				
				for (Group g: Ctx.config.getGroups()) {
					
					String regex = g.generateRegex(g.getValue());
					
					final Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(concept);
					
					if(m.lookingAt()) {
						concept = g.getName();
						break;
					}
					
				}
				

				Float value = Float.parseFloat(getCellValue(row, expensesColumn));
				Float total = map.containsKey(concept) ? value + map.get(concept) : value;

				map.put(concept, round(total));
			}

		}
	}
}
