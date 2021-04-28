package br.com.a2dm.spdm.bean;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletResponse;

import br.com.a2dm.brcmn.dto.PedidoDTO;
import br.com.a2dm.brcmn.dto.ProdutoDTO;
import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.brcmn.util.jsf.JSFUtil;
import br.com.a2dm.brcmn.util.jsf.Variaveis;
import br.com.a2dm.brcmn.util.validacoes.ValidaPermissao;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Cliente;
import br.com.a2dm.spdm.entity.OpcaoEntrega;
import br.com.a2dm.spdm.entity.Pedido;
import br.com.a2dm.spdm.entity.PedidoProduto;
import br.com.a2dm.spdm.entity.Produto;
import br.com.a2dm.spdm.omie.service.OmiePedidoService;
import br.com.a2dm.spdm.omie.service.OmieProdutoService;
import br.com.a2dm.spdm.service.OpcaoEntregaService;
import br.com.a2dm.spdm.service.PedidoService;
import br.com.a2dm.spdm.utils.DateUtils;

@RequestScoped
@ManagedBean
public class PedidoBean extends AbstractBean<Pedido, PedidoService>
{
	private Integer tpPesquisaProduto;
	private BigInteger filtroCodigoProduto;	
	private String filtroDescricaoProduto;	
	private BigInteger codigoProduto;	
	private Produto produto;
	private Produto produtoSelecionado;
	private BigInteger qtdSolicitada;
	private BigInteger idProdutoRemover;
	private String stringData;
	private String stringDataEntrega;
	private List<Produto> listaProduto;
	private List<Produto> listaProdutoResult;
	private List<OpcaoEntrega> listaOpcaoEntrega;
	private OpcaoEntrega opcaoEntrega;
	private PedidoDTO pedidoDTO;
	private BigInteger idCodigoPedidoSelecionado;
	private BigInteger idCodigoPedidoIntegracaoSelecionado;
	private BigInteger idClienteSelecionado;
	
	private final String LISTA_PRODUTOS_SESSAO = "produtos";
	
	private Pedido pedido;
	private BigInteger idPedidoInativar;
	
	private JSFUtil util = new JSFUtil();
	
	public PedidoBean()
	{
		super(PedidoService.getInstancia());
		this.ACTION_SEARCH = "pedido";
		this.pageTitle = "Pedido";
		
		MenuControl.ativarMenu("flgMenuPed");		
	}
	
	@Override
	protected void setValoresDefault() throws Exception
	{
		this.setPedido(new Pedido());
		this.getSearchObject().setDatPedido(new Date());
		this.atualizarStringData(this.getSearchObject().getDatPedido());
	}
	
	@Override
	protected void setDefaultInserir() throws Exception
	{		
		this.getEntity().setDatPedido(new Date());
		this.setTpPesquisaProduto(1);
		this.setProduto(new Produto());
		this.setListaProdutoResult(new ArrayList<Produto>());
		this.atualizarFiltroProduto();
		this.atualizarStringDataInsert();
	}
	
	@Override
	protected void setListaInserir() throws Exception
	{
		this.iniciaListaProdutos();
		
		produto.setFiltroMap(new HashMap<String, Object>());
		produto.getFiltroMap().put("flgAtivoClienteProduto", "S");
		produto.getFiltroMap().put("idCliente", util.getUsuarioLogado().getIdCliente());
		
		List<ProdutoDTO> listaProduto = OmieProdutoService.getInstance().listarProdutosPorCliente(util.getUsuarioLogado().getIdCliente());
		
		if (listaProduto != null
				&& listaProduto.size() > 0) {
			for (ProdutoDTO element: listaProduto) { 
				Produto produto = new Produto();
				produto.setIdProduto(element.getIdProduto());
				produto.setDesProduto(element.getDesProduto());
				produto.setValorUnitario(element.getValorUnitario());
				produto.setQtdLoteMinimo(element.getQtdLoteMinimo());
				produto.setQtdMultiplo(element.getQtdMultiplo());
				
				if (produto.getValorUnitario() != null 
						&& produto.getValorUnitario().doubleValue() > 0) {
					this.getListaProduto().add(produto);
				}
			}
		}
		
		this.iniciaListaOpcaoEntrega();
		
		OpcaoEntrega opcaoEntrega = new OpcaoEntrega();
		opcaoEntrega.setFlgAtivo("S");
		List<OpcaoEntrega> listaOpcaoEntrega = OpcaoEntregaService.getInstancia().pesquisar(opcaoEntrega, 0);
		this.getListaOpcaoEntrega().addAll(listaOpcaoEntrega);
	}
	
	public void buscarProdutos() throws Exception {
		this.setProduto(new Produto());
		this.iniciaListaProdutos();
		
		if (this.getEntity().getIdCliente() != null) 
		{
			List<ProdutoDTO> listaProduto = OmieProdutoService.getInstance().listarProdutosPorCliente(this.getEntity().getIdCliente());
			
			if (listaProduto != null
					&& listaProduto.size() > 0) {
				for (ProdutoDTO element: listaProduto) { 
					Produto produto = new Produto();
					produto.setIdProduto(element.getIdProduto());
					produto.setDesProduto(element.getDesProduto());
					produto.setValorUnitario(element.getValorUnitario());
					produto.setQtdLoteMinimo(element.getQtdLoteMinimo());
					produto.setQtdMultiplo(element.getQtdMultiplo());
					
					if (produto.getValorUnitario() != null 
							&& produto.getValorUnitario().doubleValue() > 0) {
						this.getListaProduto().add(produto);
					}
				}
			}
		}
	}
	
	@Override
	public void pesquisar(ActionEvent event)
    {	   
		try
		{
			if(validarAcesso(Variaveis.ACAO_PESQUISAR))
			{
				validarPesquisar();
				completarPesquisar();
				
				String dateStr = DateUtils.formatDate(this.getSearchObject().getDatPedido(),"yyyy-MM-dd");
				PedidoDTO pedidoDTO = OmiePedidoService.getInstance().pesquisarPedido(util.getUsuarioLogado().getIdCliente(), 
																					 this.getSearchObject().getIdPedido(), 
																					 dateStr);
				
				setPedidoDTO(pedidoDTO);
				adicionarPedidoDTO();
				completarPosPesquisar();
				setCurrentState(STATE_SEARCH);
			}
		}
		catch (Exception e)
		{
			FacesMessage message = new FacesMessage(e.getMessage());
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			if(e.getMessage() == null)	
				FacesContext.getCurrentInstance().addMessage("", message);
			else
				FacesContext.getCurrentInstance().addMessage(null, message);
			this.setPedido(new Pedido());
		}
    }
	
	private void adicionarPedidoDTO() {
		List<Pedido> lista = new ArrayList<>();
		Pedido pedido = new Pedido();
		pedido.setIdPedido(pedidoDTO.getIdPedido());
		pedido.setStringData(DateUtils.formatDatePtBr(pedidoDTO.getDataPedido()));
		pedido.setObservacao(pedidoDTO.getObservacao());
		pedido.setIdCodigoPedido(pedidoDTO.getCodigoPedido());
		pedido.setIdCodigoPedidoIntegracao(pedidoDTO.getCodigoPedidoIntegracao());
		pedido.setIdOpcaoEntrega(pedidoDTO.getIdOpcaoEntrega());
		pedido.setFlgAtivo(pedidoDTO.getFlgAtivo());
		pedido.setListaPedidoProduto(new ArrayList<>());
		
		if (pedidoDTO.getProdutos() != null
				&& pedidoDTO.getProdutos().size() > 0) { 
			for (ProdutoDTO element: pedidoDTO.getProdutos()) {
				PedidoProduto pedidoProduto = new PedidoProduto();
				pedidoProduto.setProduto(new Produto());
				pedidoProduto.getProduto().setDesProduto(element.getDesProduto());
				pedidoProduto.getProduto().setFlgAtivo("S");
				pedidoProduto.getProduto().setValorUnitario(element.getValorUnitario());
				pedidoProduto.setQtdSolicitada(element.getQtdSolicitada());
				
				pedido.getListaPedidoProduto().add(pedidoProduto);
			}
		}
		lista.add(pedido);
		this.setSearchResult(lista);
	}
	
	@Override
	protected void validarInserir() throws Exception
	{
		if(this.getEntity() == null
				|| this.getEntity().getDatPedido() == null
				|| this.getEntity().getDatPedido().toString().trim().equals(""))
		{
			throw new Exception("O campo Data da Produção é obrigatório!");
		}
		
		if (this.getEntity().getIdOpcaoEntrega() == null
				|| this.getEntity().getIdOpcaoEntrega().intValue() <= 0) 
		{
			throw new Exception("O campo Opção de Entrega é obrigatório!");
		} 
		
		if(this.getListaProdutoResult() == null
				|| this.getListaProdutoResult().size() <= 0)
		{
			throw new Exception("Pelo menos 1 produto deve ser adicionado ao pedido!");
		}
		
		Calendar c = Calendar.getInstance();
		
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Date dataHoje = c.getTime();
		
		if(this.getEntity().getDatPedido().before(dataHoje))
		{
			throw new Exception("O campo Data da Produção não pode ser menor que a Data Atual!");
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.getEntity().getDatPedido());
		
		int dia = calendar.get(Calendar.DAY_OF_WEEK);
		
		if (dia == Calendar.SUNDAY) 
		{
			throw new Exception("Não é possível realizar pedido para o dia de Domingo!");
		}
	}

	public void preparaAlterar()
	{
		try
		{
			if(validarAcesso(Variaveis.ACAO_PREPARA_ALTERAR))
			{
				util.getSession().removeAttribute(LISTA_PRODUTOS_SESSAO);
				
				this.getEntity().setIdPedido(pedidoDTO.getIdPedido());
				this.getEntity().setObsPedido(pedidoDTO.getObservacao());
				this.getEntity().setFlgAtivo(pedidoDTO.getFlgAtivo());
				this.getEntity().setDatPedido(pedidoDTO.getDataPedido());
				this.getEntity().setIdOpcaoEntrega(pedidoDTO.getIdOpcaoEntrega());
				this.getEntity().setIdCodigoPedidoIntegracao(pedidoDTO.getCodigoPedidoIntegracao());
				
				this.setTpPesquisaProduto(1);
				this.setProduto(new Produto());
				this.setListaProdutoResult(new ArrayList<Produto>());
				this.atualizarFiltroProduto();
				
				this.setListaProdutoResult(new ArrayList<Produto>());
				List<Produto> listaProdutoSessao = new ArrayList<Produto>();				
				
				if (pedidoDTO.getProdutos() != null
						&& pedidoDTO.getProdutos().size() > 0) { 
					for (ProdutoDTO element: pedidoDTO.getProdutos()) {
						Produto produto = new Produto();
						produto.setIdProduto(element.getIdProduto());
						produto.setDesProduto(element.getDesProduto());
						produto.setValorUnitario(element.getValorUnitario());
						produto.setFlgAtivo("S");
						produto.setQtdSolicitada(element.getQtdSolicitada());
						
						this.getListaProdutoResult().add(produto);
						listaProdutoSessao.add(produto);
					}
				}
				
				if (this.getEntity().getOpcaoEntrega() == null) {
					this.getEntity().setOpcaoEntrega(new OpcaoEntrega());
				}
				
				util.getSession().setAttribute(LISTA_PRODUTOS_SESSAO, listaProdutoSessao);
				
				// SETANDO LISTA DE OPCAO DE ENTREGA
				OpcaoEntrega opcaoEntrega = new OpcaoEntrega();
				opcaoEntrega.setFlgAtivo("S");
				
				List<OpcaoEntrega> listaOpcaoEntrega = OpcaoEntregaService.getInstancia().pesquisar(opcaoEntrega, 0);
				this.iniciaListaOpcaoEntrega();
				this.getListaOpcaoEntrega().addAll(listaOpcaoEntrega);
				
				setCurrentState(STATE_EDIT);
				setListaAlterar();
			}
		}
		catch (Exception e)
		{
			FacesMessage message = new FacesMessage(e.getMessage());
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			if(e.getMessage() == null)
				FacesContext.getCurrentInstance().addMessage("", message);
			else
				FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	private void iniciaListaOpcaoEntrega()
	{
		ArrayList<OpcaoEntrega> lista = new ArrayList<>();
		OpcaoEntrega opcaoEntrega = new OpcaoEntrega();
		opcaoEntrega.setIdOpcaoEntrega(null);
		opcaoEntrega.setDesOpcaoEntrega("Escolha a Opção de Entrega");		
		lista.add(opcaoEntrega);
		
		this.setListaOpcaoEntrega(lista);
	}
	
	@Override
	protected void validarPesquisar() throws Exception
	{
		if((this.getSearchObject().getIdPedido() == null
				|| this.getSearchObject().getIdPedido().intValue() <= 0)
				|| (this.getSearchObject().getDatPedido() == null
					|| this.getSearchObject().getDatPedido().toString().trim().equals("")))
		{
			throw new Exception("Os campos com * são obrigatórios!");
		}
	}
	
	public void atualizarFiltroProduto()
	{
		this.setFiltroCodigoProduto(null);
		this.setFiltroDescricaoProduto(null);
		this.setCodigoProduto(null);
		this.getProduto().setDesProduto(null);
		this.getProduto().setIdProduto(null);
		this.setQtdSolicitada(null);
		this.iniciaListaProdutos();
	}
	
	private void iniciaListaProdutos()
	{
		ArrayList<Produto> lista = new ArrayList<Produto>();
		Produto produto = new Produto();
		produto.setIdProduto(null);
		produto.setDesProduto("Escolha o Produto");		
		lista.add(produto);
		
		this.setListaProduto(lista);
	}
	
	@Override
	protected void completarAlterar() throws Exception
	{
		this.getEntity().setCliente(new Cliente());
		this.getEntity().getCliente().setIdCliente(util.getUsuarioLogado().getIdCliente());
		this.getEntity().getCliente().setListaProduto(this.getListaProdutoResult());
		this.getEntity().setFlgAtivo("S");
	}
	
	@Override
	protected void completarInserir() throws Exception
	{
		this.getEntity().setCliente(new Cliente());
		this.getEntity().getCliente().setIdCliente(util.getUsuarioLogado().getIdCliente());
		this.getEntity().getCliente().setListaProduto(this.getListaProdutoResult());
		this.getEntity().setFlgAtivo("S");
	}
	
	public void alterar(ActionEvent event) 
	   {
	      try
	      {
	    	  if(validarAcesso(Variaveis.ACAO_ALTERAR))
	    	  {
	    		  completarAlterar();
	    		  PedidoService.getInstancia().alterarPedido(getEntity());
	    		  FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Pedido alterado com sucesso.", null));
	    		  
	    		  this.cancelar(event);
	    	  }
	      }
	      catch (Exception e)
	      {
	         FacesMessage message = new FacesMessage(e.getMessage());
	         message.setSeverity(FacesMessage.SEVERITY_ERROR);
	         if(e.getMessage() == null)
	        	 FacesContext.getCurrentInstance().addMessage("", message);
	         else
	        	 FacesContext.getCurrentInstance().addMessage(null, message);
	      }
	   }
	
   public void inserir(ActionEvent event) 
   {
      try
      {
    	  if(validarAcesso(Variaveis.ACAO_INSERIR))
    	  {
    		  completarInserir();
    		  PedidoDTO pedidoDTO = PedidoService.getInstancia().inserirPedido(getEntity());
    		  FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Pedido inserido com sucesso. Protocolo do Pedido: "+ pedidoDTO.getIdPedido(), null));
    		  
    		  this.cancelar(event);
    	  }
      }
      catch (Exception e)
      {
         FacesMessage message = new FacesMessage(e.getMessage());
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
         if(e.getMessage() == null)
        	 FacesContext.getCurrentInstance().addMessage("", message);
         else
        	 FacesContext.getCurrentInstance().addMessage(null, message);
      }
   }
	
	public void adicionarProduto(ActionEvent event)
	{
		try
		{
			//PRODUTO E QUANTIDADE OBRIGATORIOS
			if(this.getProduto() == null
					|| this.getProduto().getIdProduto() == null
					|| this.getProduto().getIdProduto().intValue() <= 0)
			{
				throw new Exception("O campo Produto é obrigatório!");			
			}
			
			if(this.getQtdSolicitada() == null
					|| this.getQtdSolicitada().intValue() <= 0)
			{
				throw new Exception("O campo Quantidade é obrigatório!");			
			}
			
			//VALIDAR PRODUTO EXISTENTE
			if(this.listaProdutoResult == null)
			{
				this.setListaProdutoResult(new ArrayList<Produto>());
			}
			else
			{
				for (Produto produto : listaProdutoResult)
				{
					if(this.getProduto().getIdProduto().intValue() == produto.getIdProduto().intValue())
					{
						throw new Exception("Este produto já está adicionado na lista!");
					}
				}
			}
			
			//ADICIONANDO O PRODUTO
			Produto produto = new Produto();
			produto.setIdProduto(this.getProduto().getIdProduto());
			
			Optional<Produto> produtoOptional = this.getListaProduto().stream()
                    .filter(x -> x.getIdProduto() == this.getProduto().getIdProduto())
                    .findFirst();
			
			produto.setValorUnitario(produtoOptional.get().getValorUnitario());
			produto.setDesProduto(produtoOptional.get().getDesProduto());
			produto.setQtdSolicitada(this.getQtdSolicitada());
			produto.setFlgAtivo("S");
			
			this.getListaProdutoResult().add(0, produto);
			this.getProduto().setIdProduto(null);
			this.setQtdSolicitada(null);
			this.setProdutoSelecionado(null);
		}
		catch (Exception e)
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	public void removerProduto()
	{
		if(this.getListaProdutoResult() != null)
		{
			for (Produto produto : listaProdutoResult)
			{
				if(produto.getIdProduto().intValue() == this.getIdProdutoRemover().intValue())
				{
					produto.setFlgAtivo("N");
					return;
				}
			}
		}
	}
	
	public void inativar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				if(validarAcesso(Variaveis.ACAO_INATIVAR))
				{
					PedidoDTO pedidoDTO = new PedidoDTO();
					pedidoDTO.setCodigoPedidoIntegracao(getPedidoDTO().getCodigoPedidoIntegracao()); 
					pedidoDTO.setCodigoPedido(getPedidoDTO().getCodigoPedido());
					OmiePedidoService.getInstance().inativarPedido(this.getIdClienteSelecionado(), pedidoDTO);

					this.setSearchResult(new ArrayList<>());
					FacesMessage message = new FacesMessage("Pedido cancelado com sucesso!");
					message.setSeverity(FacesMessage.SEVERITY_INFO);
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	public void atualizarStringDataInsert()
	{
		String nome = this.atualizarStringData(this.getEntity().getDatPedido());
		this.atualizarStringDataEntrega(this.getEntity().getDatPedido(), nome);
	}
	
	public void atualizarStringDataSearch()
	{
		this.atualizarStringData(this.getSearchObject().getDatPedido());
	}
	
	public void atualizarStringDataEntrega(Date data, String nome) {
		Calendar c = Calendar.getInstance(); 
		c.setTime(data);
		
		if (nome.equalsIgnoreCase("Sábado")) {
			c.add(Calendar.DATE, 2);
		} else {
			c.add(Calendar.DATE, 1);
		}
		
		int dia = c.get(Calendar.DAY_OF_WEEK);
		
		switch(dia)
		{
		  case Calendar.SUNDAY: nome = "Domingo";break;
		  case Calendar.MONDAY: nome = "Segunda-feira";break;
		  case Calendar.TUESDAY: nome = "Terça-feira";break;
		  case Calendar.WEDNESDAY: nome = "Quarta-feira";break;
		  case Calendar.THURSDAY: nome = "Quinta-feira";break;
		  case Calendar.FRIDAY: nome = "Sexta-feira";break;
		  case Calendar.SATURDAY: nome = "Sábado";break;
		}
		
		this.setStringDataEntrega(nome);
	}
	
	public String atualizarStringData(Date data)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(data);
		
		String nome = "";
		
		int dia = calendar.get(Calendar.DAY_OF_WEEK);
		
		switch(dia)
		{
		  case Calendar.SUNDAY: nome = "Domingo";break;
		  case Calendar.MONDAY: nome = "Segunda-feira";break;
		  case Calendar.TUESDAY: nome = "Terça-feira";break;
		  case Calendar.WEDNESDAY: nome = "Quarta-feira";break;
		  case Calendar.THURSDAY: nome = "Quinta-feira";break;
		  case Calendar.FRIDAY: nome = "Sexta-feira";break;
		  case Calendar.SATURDAY: nome = "Sábado";break;
		}
				
		this.setStringData(nome);
		return nome;
	}
	
	public void buscarInformacoes() throws Exception {
		if (this.getProduto().getIdProduto() != null && this.getProduto().getIdProduto().intValue() > 0) {
			Optional<Produto> produtoOptional = this.getListaProduto().stream()
					                                   .filter(x -> x.getIdProduto() == this.getProduto().getIdProduto())
					                                   .findFirst();
			
			if (produtoOptional.isPresent()) {
				produtoSelecionado = produtoOptional.get();
			}
					                                
		} else {
			setProdutoSelecionado(null);
		}
	}
	
	public void buscarInformacoesOpcaoEntrega() throws Exception
	{
		String vlrFreteFormatado = PedidoService.getInstancia().buscarInformacoesOpcaoEntrega(util.getUsuarioLogado().getIdCliente(), this.getEntity().getIdOpcaoEntrega());
		this.getEntity().setVlrFreteFormatado(vlrFreteFormatado);
	}
	
	@Override
	protected boolean validarAcesso(String acao)
	{
		boolean temAcesso = true;

		if (!ValidaPermissao.getInstancia().verificaPermissao("pedido", acao))
		{
			temAcesso = false;
			HttpServletResponse rp = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
			try
			{
				rp.sendRedirect("/spdm/pages/acessoNegado.jsf");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return temAcesso;
	}
	
	@Override
	public void cancelar(ActionEvent event)
	{
		this.atualizarStringData(this.getSearchObject().getDatPedido());
		this.getSearchObject().setDatPedido(new Date());		
	}

	public Integer getTpPesquisaProduto() {
		return tpPesquisaProduto;
	}

	public void setTpPesquisaProduto(Integer tpPesquisaProduto) {
		this.tpPesquisaProduto = tpPesquisaProduto;
	}

	public BigInteger getFiltroCodigoProduto() {
		return filtroCodigoProduto;
	}

	public void setFiltroCodigoProduto(BigInteger filtroCodigoProduto) {
		this.filtroCodigoProduto = filtroCodigoProduto;
	}

	public String getFiltroDescricaoProduto() {
		return filtroDescricaoProduto;
	}

	public void setFiltroDescricaoProduto(String filtroDescricaoProduto) {
		this.filtroDescricaoProduto = filtroDescricaoProduto;
	}

	public BigInteger getCodigoProduto() {
		return codigoProduto;
	}

	public void setCodigoProduto(BigInteger codigoProduto) {
		this.codigoProduto = codigoProduto;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public List<Produto> getListaProduto() {
		return listaProduto;
	}

	public void setListaProduto(List<Produto> listaProduto) {
		this.listaProduto = listaProduto;
	}
	
	public boolean isProcurarPorCodigo() {
		return (this.tpPesquisaProduto == 1 ? true : false);
	}

	public BigInteger getQtdSolicitada() {
		return qtdSolicitada;
	}

	public void setQtdSolicitada(BigInteger qtdSolicitada) {
		this.qtdSolicitada = qtdSolicitada;
	}

	public List<Produto> getListaProdutoResult() {
		return listaProdutoResult;
	}

	public void setListaProdutoResult(List<Produto> listaProdutoResult) {
		this.listaProdutoResult = listaProdutoResult;
	}

	public BigInteger getIdProdutoRemover() {
		return idProdutoRemover;
	}

	public void setIdProdutoRemover(BigInteger idProdutoRemover) {
		this.idProdutoRemover = idProdutoRemover;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public BigInteger getIdPedidoInativar() {
		return idPedidoInativar;
	}

	public void setIdPedidoInativar(BigInteger idPedidoInativar) {
		this.idPedidoInativar = idPedidoInativar;
	}

	public String getStringData() {
		return stringData;
	}

	public void setStringData(String stringData) {
		this.stringData = stringData;
	}

	public Produto getProdutoSelecionado() {
		return produtoSelecionado;
	}

	public void setProdutoSelecionado(Produto produtoSelecionado) {
		this.produtoSelecionado = produtoSelecionado;
	}

	public String getStringDataEntrega() {
		return stringDataEntrega;
	}

	public void setStringDataEntrega(String stringDataEntrega) {
		this.stringDataEntrega = stringDataEntrega;
	}

	public List<OpcaoEntrega> getListaOpcaoEntrega() {
		return listaOpcaoEntrega;
	}

	public void setListaOpcaoEntrega(List<OpcaoEntrega> listaOpcaoEntrega) {
		this.listaOpcaoEntrega = listaOpcaoEntrega;
	}

	public OpcaoEntrega getOpcaoEntrega() {
		return opcaoEntrega;
	}

	public void setOpcaoEntrega(OpcaoEntrega opcaoEntrega) {
		this.opcaoEntrega = opcaoEntrega;
	}

	public PedidoDTO getPedidoDTO() {
		return pedidoDTO;
	}

	public void setPedidoDTO(PedidoDTO pedidoDTO) {
		this.pedidoDTO = pedidoDTO;
	}

	public BigInteger getIdCodigoPedidoSelecionado() {
		return idCodigoPedidoSelecionado;
	}

	public void setIdCodigoPedidoSelecionado(BigInteger idCodigoPedidoSelecionado) {
		this.idCodigoPedidoSelecionado = idCodigoPedidoSelecionado;
	}

	public BigInteger getIdCodigoPedidoIntegracaoSelecionado() {
		return idCodigoPedidoIntegracaoSelecionado;
	}

	public void setIdCodigoPedidoIntegracaoSelecionado(BigInteger idCodigoPedidoIntegracaoSelecionado) {
		this.idCodigoPedidoIntegracaoSelecionado = idCodigoPedidoIntegracaoSelecionado;
	}

	public BigInteger getIdClienteSelecionado() {
		return idClienteSelecionado;
	}

	public void setIdClienteSelecionado(BigInteger idClienteSelecionado) {
		this.idClienteSelecionado = idClienteSelecionado;
	}
}