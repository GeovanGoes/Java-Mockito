/**
 * 
 */
package br.com.caelum.leilao.infra.email;

import br.com.caelum.leilao.dominio.Leilao;

/**
 * @author geovan.goes
 *
 */
public interface EnviadorDeEmail {
    void envia(Leilao leilao);
}
