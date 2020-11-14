package AverageJoes.utils

import java.text.SimpleDateFormat
import java.util.Date

object DateUtils {

  implicit def stringToDate(stringDate: String): Date = new SimpleDateFormat("dd/MM/yyyy").parse(stringDate)

}