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

	private static final int BALANCE_COLUMN = 5;
	private static final int CONCEPT_COLUMN = 2;
	private static final int EXPENSES_COLUMN = 3;
	private static final int DATE_COLUMN = 0;
	
	private File file;
	private String balance;
	private String lastBalance;
	
	public Processor(File file) {
		this.file = file;
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

		if (row.getRowNum() > 0) {

			String date = getCellValue(row, DATE_COLUMN).substring(0, 7) + "-01";

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
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
						LinkedHashMap<String, Map<String, Float>>::new));
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
		balance = getCellValue(row, BALANCE_COLUMN);

		if (row.getRowNum() > 0 && !balance.equals(lastBalance)) {

			lastBalance = balance;

			String concept = getCellValue(row, CONCEPT_COLUMN);

			Float value = Float.parseFloat(getCellValue(row, EXPENSES_COLUMN));
			Float total = map.containsKey(concept) ? value + map.get(concept) : value;
			
			map.put(concept, round(total));

		}
	}
}
