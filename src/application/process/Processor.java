package application.process;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javafx.concurrent.Task;

public abstract class Processor<T> extends Task<T> {

	protected static final int BALANCE_COLUMN = 5;
	protected static final int CONCEPT_COLUMN = 2;
	protected static final int EXPENSES_COLUMN = 3;
	protected static final int DATE_COLUMN = 0;
	
	protected File file;
	protected double max;
	protected int current;
	protected String balance;
	protected String lastBalance;
	protected Iterator<Row> iterator;
	
	public Processor(File file) {
		this.file = file;
	}

	protected static float round(float d) {
		return new BigDecimal(Float.toString(d)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
	}

	protected static String getCellValue(Row row, int column) {
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

	protected void initializeIterator(File file) throws EncryptedDocumentException, IOException {
		Sheet sheet = WorkbookFactory.create(file).getSheetAt(0);
		this.max = sheet.getPhysicalNumberOfRows();
		this.current = 1;
		iterator = sheet.rowIterator();
	}
	
	protected void map(Row row, Map<String, Float> map) {
		balance = getCellValue(row, BALANCE_COLUMN);

		if (row.getRowNum() > 0 && !balance.equals(lastBalance)) {

			lastBalance = balance;

			String concept = getCellValue(row, CONCEPT_COLUMN);

			Float value = Float.parseFloat(getCellValue(row, EXPENSES_COLUMN));
			Float total = map.containsKey(concept) ? value + map.get(concept) : value;
			
			map.put(concept, round(total));

		}
		
		//return map;
	}
}
