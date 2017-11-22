/**
 * 
 */
package br.com.caelum.leilao.servico;

import java.util.Arrays;
import java.util.Calendar;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.Relogio;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Matchers.any;

/**
 * @author geovan.goes
 *
 */
public class GeradorDePagamentoTest 
{
	
	@Test
	public void gerarPagamentoParaUmLeilaoEncerrado()
	{
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Avaliador avaliador = new Avaliador();
		
		Leilao leilao = new CriadorDeLeilao()
						.para("PS5")
						.lance(new Usuario("Ze da dega"), 2000)
						.lance(new Usuario("Zefa"), 2500)
						.constroi();
		
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		
		GeradorDePagamento geradorDePagamento = new GeradorDePagamento(leiloes, pagamentos, avaliador);
		geradorDePagamento.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);
	}
	
	
	@Test
	public void deveEmpurrarParaProximoDiaUtil()
	{
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Relogio relogio = mock(Relogio.class);
		
		Leilao leilao = new CriadorDeLeilao()
					.para("PS5")
					.lance(new Usuario("Ze da dega"), 2000)
					.lance(new Usuario("Zefa"), 2500)
					.constroi();
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		
		Calendar sabado = Calendar.getInstance();
		sabado.set(2012, Calendar.APRIL, 7);
		
		when(relogio.hoje()).thenReturn(sabado);
		
		GeradorDePagamento geradorDePagamento = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
		geradorDePagamento.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
		assertEquals(9, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
	}

}
