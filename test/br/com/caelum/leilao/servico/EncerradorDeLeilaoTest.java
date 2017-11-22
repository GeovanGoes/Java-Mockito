/**
 * 
 */
package br.com.caelum.leilao.servico;

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
import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.email.EnviadorDeEmail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * @author geovan.goes
 *
 */
public class EncerradorDeLeilaoTest {

	@Before
	public void before()
	{
		
	}
	
	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes()
	{
		Calendar instance = Calendar.getInstance();
		instance.set(1999,1,20);
		
		Leilao leilao = new CriadorDeLeilao().para("Tv de plasma").naData(instance).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(instance).constroi();

		List<Leilao> leiloesAntigos = Arrays.asList(leilao, leilao2);
		
		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		when(dao.correntes()).thenReturn(leiloesAntigos);
		
        EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
        
        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, enviadorDeEmail);
        encerrador.encerra();
		
		assertTrue(leilao.isEncerrado());
		assertTrue(leilao2.isEncerrado());
		assertEquals(2, encerrador.getTotalEncerrados());
	}
	
	@Test
	public void leiloesQueComecaramOntem()
	{
		Calendar ontem = Calendar.getInstance();
		ontem.add(Calendar.DAY_OF_MONTH, -1);
		
		Leilao leilao =  new CriadorDeLeilao().para("Copos").naData(ontem).constroi();
		
		List<Leilao> leiloesDeOntem =  Arrays.asList(leilao);
		
		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		when(dao.correntes()).thenReturn(leiloesDeOntem);
		
        EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
        
        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, enviadorDeEmail);
        encerrador.encerra();
		
		assertFalse(leilao.isEncerrado());
	}
	
	@Test
	public void quandoNaoTemLeiloes()
	{
		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		when(dao.correntes()).thenReturn(new ArrayList<Leilao>());
		
        EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
        
        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, enviadorDeEmail);
        encerrador.encerra();
		
		assertEquals(0, encerrador.getTotalEncerrados());
	}
	
	@Test
	public void deveAtualizarLeiloesEncerrados()
	{
		Calendar antiga = Calendar.getInstance();
		antiga.add(Calendar.YEAR, -19);
		
		Leilao leilao = new CriadorDeLeilao().para("Tv").naData(antiga).constroi();
		
		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		when(dao.correntes()).thenReturn(Arrays.asList(leilao));
		
        EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
        
        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, enviadorDeEmail);
        encerrador.encerra();
		
		verify(dao, times(1)).atualiza(leilao);
	}
	
	@Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {

        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
            .naData(ontem).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
            .naData(ontem).constroi();

        RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        
        EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
        
        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, enviadorDeEmail);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());

        verify(daoFalso, never()).atualiza(leilao1);
        verify(daoFalso, never()).atualiza(leilao2);
    }
	
	@Test
	public void verificarSeEhEnviadoPorEmail()
	{
		Calendar antiga = Calendar.getInstance();
		antiga.add(Calendar.YEAR, -19);
		
		Leilao leilao = new CriadorDeLeilao().para("Copos").naData(antiga).constroi();
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao));

        EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
        
        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, enviadorDeEmail);
        encerrador.encerra();
		
		InOrder inOrder = inOrder(daoFalso,enviadorDeEmail);
		
		inOrder.verify(daoFalso, times(1)).atualiza(leilao);
		inOrder.verify(enviadorDeEmail, times(1)).envia(leilao);
	}
	
	
	@Test
	public void deveContinuarMesmoComFalhaNoDao()
	{
		Calendar antiga = Calendar.getInstance();
		antiga.add(Calendar.YEAR, -19);
		
		Leilao leilao = new CriadorDeLeilao().para("Copos").naData(antiga).constroi();
		Leilao leilao1 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao, leilao1));
        doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao);
        
        EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
        
        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, enviadorDeEmail);
        encerrador.encerra();
		
        verify(daoFalso, times(1)).atualiza(leilao1);
        verify(enviadorDeEmail, times(1)).envia(leilao1);
        
        verify(enviadorDeEmail, never()).envia(leilao);
	}
	
	
	@Test
	public void nuncaEnviarEmail()
	{
		Calendar antiga = Calendar.getInstance();
		antiga.add(Calendar.YEAR, -19);
		
		Leilao leilao = new CriadorDeLeilao().para("Copos").naData(antiga).constroi();
		Leilao leilao1 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao, leilao1));
        doThrow(new RuntimeException()).when(daoFalso).atualiza(any(Leilao.class));
        
        EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
        
        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, enviadorDeEmail);
        encerrador.encerra();
        
        verify(enviadorDeEmail, never()).envia(any(Leilao.class));
        verify(enviadorDeEmail, never()).envia(any(Leilao.class));
	}
	
	
	
}
