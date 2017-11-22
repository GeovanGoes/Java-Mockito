/**
 * 
 */
package br.com.caelum.leilao.infra;

import java.util.Calendar;

/**
 * @author geovan.goes
 *
 */
public class RelogioDoSistema implements Relogio {
	public Calendar hoje() {
		return Calendar.getInstance();
	}
}
