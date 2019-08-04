package application.process;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class Processor {

	private int balanceColumn;
	private int conceptColumn;
	private int expensesColumn;
	private int dateColumn;
	private int startRow;
	
	private File file;
	private String balance;
	private String lastBalance;
	
	public Processor(File file, int balance, int concept, int expenses, int date, int startRow) {
		this.file = file;
		this.balanceColumn = balance;
		this.conceptColumn = concept;
		this.expensesColumn = expenses;
		this.dateColumn = date;
		this.startRow = startRow;
	}

	public Map<String, Float> process() throws Exception {

		Map<String, Float> result = new HashMap<>();

		Iterator<Row> i = getIterator(file);

		while (i.hasNext()) {
			Row row = i.next();
			map(row, result);
		}

		return result;
	}

	public Map<String, Map<String, Float>> processMonthly() throws Exception {

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

			String date = getCellValue(row, dateColumn).substring(0, 7) + "-01";

			if (!source.containsKey(date)) {
				source.put(date, new HashMap<>());
			}

			result = source.get(date);
		}

		return result;
	}

	private Map<String, Map<String, Float>> sortByKey(Map<String, Map<String, Float>> input) {
		return input.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(
							e -> e.getKey(),
							e -> e.getValue(),
							(e1, e2) -> e2,
							LinkedHashMap<String, Map<String, Float>>::new
						));
	}
	
	private static float round(float d) {
		return new BigDecimal(Float.toString(d)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
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

	private Iterator<Row> getIterator(File file) throws EncryptedDocumentException, IOException {
		return WorkbookFactory.create(file).getSheetAt(0).rowIterator();
	}
	
	private void map(Row row, Map<String, Float> map) {

		boolean inRange = row.getRowNum() >= startRow - 1;

		if (inRange) {

			balance = getCellValue(row, balanceColumn);
			boolean duplicate = balance.equals(lastBalance);

			if (!duplicate) {

				lastBalance = balance;

				String concept = getCellValue(row, conceptColumn);

				/*
				 * v1.1 boolean isAmazon = Pattern
				 * .compile("(TJ-)?((AMZN MKTP ES)|(AMAZON.ES))", Pattern.CASE_INSENSITIVE)
				 * .matcher(concept) .lookingAt();
				 * 
				 * boolean isDominos = Pattern .compile("(TJ-)?DOMINOS PIZZA",
				 * Pattern.CASE_INSENSITIVE) .matcher(concept) .lookingAt();
				 * 
				 * boolean isTelepizza = Pattern .compile(".*TELEPIZZA.*",
				 * Pattern.CASE_INSENSITIVE) .matcher(concept) .lookingAt();
				 * 
				 * if(isAmazon) { concept = "Amazon"; } else if (isTelepizza) { concept =
				 * "TELEPIZZA"; } else if (isDominos) { concept = "DOMINOS PIZZA"; }
				 */

				Float value = Float.parseFloat(getCellValue(row, expensesColumn));
				Float total = map.containsKey(concept) ? value + map.get(concept) : value;

				map.put(concept, round(total));
			}

		}
	}
}
