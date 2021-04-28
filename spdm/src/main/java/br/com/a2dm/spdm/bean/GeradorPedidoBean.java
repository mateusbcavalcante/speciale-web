package br.com.a2dm.spdm.bean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import br.com.a2dm.brcmn.dto.PedidoDTO;
import br.com.a2dm.brcmn.dto.ProdutoDTO;
import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.brcmn.util.jsf.JSFUtil;
import br.com.a2dm.brcmn.util.jsf.Variaveis;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Cliente;
import br.com.a2dm.spdm.entity.OpcaoEntrega;
import br.com.a2dm.spdm.entity.Pedido;
import br.com.a2dm.spdm.entity.PedidoProduto;
import br.com.a2dm.spdm.entity.Produto;
import br.com.a2dm.spdm.omie.service.OmiePedidoService;
import br.com.a2dm.spdm.omie.service.OmieProdutoService;
import br.com.a2dm.spdm.service.ClienteService;
import br.com.a2dm.spdm.service.OpcaoEntregaService;
import br.com.a2dm.spdm.service.PedidoService;
import br.com.a2dm.spdm.utils.DateUtils;

@RequestScoped
@ManagedBean
public class GeradorPedidoBean extends AbstractBean<Pedido, PedidoService>
{
	
	private List<Cliente> listaCliente;
	private String stringData;
	private Produto produto;
	private Produto produtoSelecionado;
	private List<Produto> listaProduto;
	private Pedido pedido;
	private List<Pedido> listaPedidoResult;
	private List<OpcaoEntrega> listaOpcaoEntrega;
	private PedidoDTO pedidoDTO;
	
	private JSFUtil util = new JSFUtil();
	private BigInteger qtdSolicitada;
	private BigInteger idPedidoSelecionado;
	private BigInteger idCodigoPedidoSelecionado;
	private BigInteger idCodigoPedidoIntegracaoSelecionado;
	private BigInteger idClienteSelecionado;
	private BigInteger idClienteRemover;
	private BigInteger idProdutoRemover;
	private String desClienteSelecionado;
	private Date datPedidoSelecionada;
	
	public GeradorPedidoBean()
	{
		super(PedidoService.getInstancia());
		this.ACTION_SEARCH = "geradorPedido";
		this.pageTitle = "Gerador de Pedido";
		
		MenuControl.ativarMenu("flgMenGerPed");		
	}
	
	@Override
	protected void completarPesquisar() throws Exception
	{
		if(this.getSearchObject().getFlgAtivo() != null && this.getSearchObject().getFlgAtivo().equalsIgnoreCase("T"))
		{
			this.getSearchObject().setFlgAtivo(null);
		}
	}
	
	@Override
	protected void setValoresDefault() throws Exception
	{
		//DATA DE HOJE
		Calendar c = Calendar.getInstance();
		
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		this.getSearchObject().setDatPedido(c.getTime());		
		
		//CARREGANDO LISTA DE CLIENTES		
		Cliente cliente = new Cliente();
		cliente.setFlgAtivo("S");		
		List<Cliente> resultCli = ClienteService.getInstancia().pesquisar(cliente, 0);
		
		Cliente cli = new Cliente();
		cli.setDesCliente("Todos os Clientes");
		
		List<Cliente> listaCliente = new ArrayList<Cliente>();
		listaCliente.add(cli);
		listaCliente.addAll(resultCli);
		
		this.setListaCliente(listaCliente);
		this.getSearchObject().setFlgAtivo("T");
	}
	
	@Override
	protected void setDefaultInserir() throws Exception
	{		
		this.setListaPedidoResult(new ArrayList<Pedido>());
		this.setProduto(new Produto());
		this.iniciaListaOpcaoEntrega();
		
		OpcaoEntrega opcaoEntrega = new OpcaoEntrega();
		opcaoEntrega.setFlgAtivo("S");
		List<OpcaoEntrega> listaOpcaoEntrega = OpcaoEntregaService.getInstancia().pesquisar(opcaoEntrega, 0);
		this.getListaOpcaoEntrega().addAll(listaOpcaoEntrega);
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
	
	private void iniciaListaProdutos()
	{
		ArrayList<Produto> lista = new ArrayList<Produto>();
		Produto produto = new Produto();
		produto.setIdProduto(null);
		produto.setDesProduto("Escolha o Produto");		
		lista.add(produto);
		
		this.setListaProduto(lista);
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
	
	@Override
	protected void validarPesquisar() throws Exception
	{
		if((this.getSearchObject().getIdPedido() == null
				|| this.getSearchObject().getIdPedido().intValue() <= 0)
				|| (this.getSearchObject().getIdCliente() == null
						|| this.getSearchObject().getIdCliente().intValue() <= 0)
				|| (this.getSearchObject().getDatPedido() == null
					|| this.getSearchObject().getDatPedido().toString().trim().equals("")))
		{
			throw new Exception("Os campos com * são obrigatórios!");
		}
	}
	
	@Override
	public void preparaAlterar() {
		try
		{
			if(validarAcesso(Variaveis.ACAO_PREPARA_ALTERAR))
			{
				Pedido pedido = new Pedido();
				pedido.setIdUsuarioAlt(util.getUsuarioLogado().getIdUsuario());
				pedido.setIdCodigoPedidoIntegracao(this.getIdCodigoPedidoIntegracaoSelecionado());
				pedido.setDatAlteracao(new Date());
				pedido.setFlgAtivo("S");
				pedido.setIdPedido(pedidoDTO.getIdPedido());
				pedido.setObsPedido(pedidoDTO.getObservacao());
				pedido.setDatPedido(pedidoDTO.getDataPedido());
				pedido.setIdOpcaoEntrega(pedidoDTO.getIdOpcaoEntrega());
				
				Cliente cliente = new Cliente();
				cliente.setIdCliente(pedidoDTO.getIdCliente());
				cliente = ClienteService.getInstancia().get(cliente, 0);
				pedido.setCliente(cliente);
				pedido.getCliente().setListaProduto(new ArrayList<>());
				
				if (pedidoDTO.getProdutos() != null
						&& pedidoDTO.getProdutos().size() > 0) { 
					for (ProdutoDTO element: pedidoDTO.getProdutos()) {
						Produto produto = new Produto();
						produto.setIdProduto(element.getIdProduto());
						produto.setDesProduto(element.getDesProduto());
						produto.setValorUnitario(element.getValorUnitario());
						produto.setFlgAtivo("S");
						produto.setQtdSolicitada(element.getQtdSolicitada());
						pedido.getCliente().getListaProduto().add(produto);
					}
				}
				
				this.setListaPedidoResult(new ArrayList<>());
				this.getListaPedidoResult().add(0, pedido);
				
				getEntity().setIdCliente(pedidoDTO.getIdCliente());
				buscarProdutos();
				setCurrentState(STATE_EDIT);
				setListaAlterar();
				
				// SETANDO LISTA DE OPCAO DE ENTREGA
				OpcaoEntrega opcaoEntrega = new OpcaoEntrega();
				opcaoEntrega.setFlgAtivo("S");
				
				List<OpcaoEntrega> listaOpcaoEntrega = OpcaoEntregaService.getInstancia().pesquisar(opcaoEntrega, 0);
				this.iniciaListaOpcaoEntrega();
				this.getListaOpcaoEntrega().addAll(listaOpcaoEntrega);
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
	
	public void inativar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				if(validarAcesso(Variaveis.ACAO_INATIVAR))
				{
					PedidoDTO pedidoDTO = new PedidoDTO();
					pedidoDTO.setCodigoPedidoIntegracao(this.getIdCodigoPedidoIntegracaoSelecionado()); 
					pedidoDTO.setCodigoPedido(this.getIdCodigoPedidoSelecionado());
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
	
	@Override
	public void pesquisar(ActionEvent event)
	{
		try
		{
			if(validarAcesso(Variaveis.ACAO_PESQUISAR))
			{
				validarPesquisar();
				completarPesquisar();
				validarCampoTexto();
				String dateStr = DateUtils.formatDate(this.getSearchObject().getDatPedido(),"yyyy-MM-dd");
				PedidoDTO pedidoDTO = OmiePedidoService.getInstance().pesquisarPedido(this.getSearchObject().getIdCliente(), 
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
			this.setSearchResult(new ArrayList<Pedido>());
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
	
	public void adicionarClienteProduto(ActionEvent event)
	{
		try
		{
			Boolean existeCliente = false;
			//CLIENTE, PRODUTO E QUANTIDADE OBRIGATORIOS
			if(this.getEntity().getIdCliente() == null
					|| this.getEntity().getIdCliente().intValue() <= 0)
			{
				throw new Exception("O campo Cliente é obrigatório!");			
			}
			
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
			
			if(produtoSelecionado.getQtdLoteMinimo().intValue() > getQtdSolicitada().intValue())
			{
				throw new Exception("O Lote Mínimo do produto " + produtoSelecionado.getDesProduto() + " não foi atingida! Quantidade de Lote Mínimo: " + produtoSelecionado.getQtdLoteMinimo());
			}
			
			if(getQtdSolicitada().intValue() % produtoSelecionado.getQtdMultiplo().intValue() != 0)
			{
				throw new Exception("A Quantidade do produto " + produtoSelecionado.getDesProduto() + " deve ser solicitada em múltiplo de "+ produtoSelecionado.getQtdMultiplo() +"!");
			}
			
			//VALIDAR PRODUTO EXISTENTE
			if(this.listaPedidoResult == null)
			{
				this.setListaPedidoResult(new ArrayList<Pedido>());
			}
			else
			{
				for (Pedido element : listaPedidoResult)
				{
					if(element.getCliente().getIdCliente().intValue() == this.getEntity().getIdCliente().intValue())
					{
						existeCliente = true;
						for (Produto elementProduto : element.getCliente().getListaProduto()) {
							if (elementProduto.getIdProduto().intValue() == produto.getIdProduto().intValue()) {
								if (elementProduto.getFlgAtivo().equalsIgnoreCase("S")) {
									throw new Exception("Este produto já está adicionado na lista para o cliente selecionado!");
								} else {
									elementProduto.setQtdSolicitada(this.getQtdSolicitada());
									elementProduto.setFlgAtivo("S");
									return;
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
						
						element.getCliente().getListaProduto().add(produto);
					}
				}
			}
			
			if (!existeCliente) {
				Produto produto = new Produto();
				produto.setIdProduto(this.getProduto().getIdProduto());
				
				Optional<Produto> produtoOptional = this.getListaProduto().stream()
                        .filter(x -> x.getIdProduto() == this.getProduto().getIdProduto())
                        .findFirst();
				
				produto.setValorUnitario(produtoOptional.get().getValorUnitario());
				produto.setDesProduto(produtoOptional.get().getDesProduto());
				produto.setQtdSolicitada(this.getQtdSolicitada());
				produto.setFlgAtivo("S");
				
				Cliente cliente = new Cliente();
				cliente.setIdCliente(this.getEntity().getIdCliente());
				cliente = ClienteService.getInstancia().get(cliente, 0);
				
				Pedido pedido = new Pedido();
				pedido.setIdUsuarioCad(util.getUsuarioLogado().getIdUsuario());
				pedido.setDatCadastro(new Date());
				pedido.setFlgAtivo("S");
				pedido.setDatPedido(new Date());
				pedido.setCliente(cliente);
				
				if (pedido.getCliente().getListaProduto() == null) {
					pedido.getCliente().setListaProduto(new ArrayList<>());
				}
				pedido.getCliente().getListaProduto().add(produto);
			
				this.getListaPedidoResult().add(0, pedido);
			}
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
	
	@Override
	protected void validarInserir() throws Exception
	{
		Calendar c = Calendar.getInstance();
		
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Date dataHoje = c.getTime();
		
		if (getListaPedidoResult() == null || getListaPedidoResult().size() <= 0) {
			throw new Exception("Pelo menos 1 pedido deve ser adicionado!");
		}
		
		for (Pedido element : getListaPedidoResult()) {
			if (element.getIdOpcaoEntrega() == null)
			{
				throw new Exception("No pedido do cliente " + element.getCliente().getDesCliente() + ", o campo Opção é obrigatório!");
			}
			
			if (element.getDatPedido() == null
					|| element.getDatPedido().toString().trim().equals(""))
			{
				throw new Exception("No pedido do cliente " + element.getCliente().getDesCliente() + ", o campo Data da Produção é obrigatório!");
			}
			
			if(element.getDatPedido().before(dataHoje))
			{
				throw new Exception("No pedido do cliente " + element.getCliente().getDesCliente() + ", o campo Data da Produção não pode ser menor que a Data Atual!");
			}
		}
	}
	
	public void alterar(ActionEvent event) 
    {
      try
      {
    	  if(validarAcesso(Variaveis.ACAO_ALTERAR))
    	  {
    		  PedidoService.getInstancia().alterarGeradorPedido(getListaPedidoResult());
    		  FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Pedido alterado com sucesso", null));
    		  
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
	
	public void buscarInformacoesOpcaoEntrega(Pedido pedido) throws Exception
	{
		String vlrFreteFormatado = PedidoService.getInstancia().buscarInformacoesOpcaoEntrega(pedido.getCliente().getIdCliente(), pedido.getIdOpcaoEntrega());
		pedido.setVlrFreteFormatado(vlrFreteFormatado);
	}
	
	public void inserir(ActionEvent event) 
    {
      try
      {
    	  if(validarAcesso(Variaveis.ACAO_INSERIR))
    	  {
    		  validarInserir();
    		  completarInserir();
    		  PedidoService.getInstancia().inserirGeradorPedido(getListaPedidoResult());
    		  FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Pedido inserido com sucesso", null));
    		  
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
	
	public void removerPedido()
	{
		if(this.getListaPedidoResult() != null)
		{
			for (Pedido element : listaPedidoResult)
			{
				if(element.getCliente().getIdCliente().intValue() == this.getIdClienteRemover().intValue())
				{
					this.getListaPedidoResult().remove(element);
					return;
				}
			}
		}
	}
	
	public void removerProduto()
	{
		for (Pedido element : listaPedidoResult)
		{
			if(element.getCliente().getIdCliente().intValue() == this.getIdClienteRemover().intValue())
			{
				for (Produto elementProduto : element.getCliente().getListaProduto()) {
					if(elementProduto.getIdProduto().intValue() == this.getIdProdutoRemover().intValue()) 
					{
						elementProduto.setFlgAtivo("N");
						break;
					}
				}
			}
		}
	}
	
	public void atualizarStringData(Date data)
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
	}

	public String getStringData() {
		return stringData;
	}

	public void setStringData(String stringData) {
		this.stringData = stringData;
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

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public List<Cliente> getListaCliente() {
		return listaCliente;
	}

	public void setListaCliente(List<Cliente> listaCliente) {
		this.listaCliente = listaCliente;
	}

	public Produto getProdutoSelecionado() {
		return produtoSelecionado;
	}

	public void setProdutoSelecionado(Produto produtoSelecionado) {
		this.produtoSelecionado = produtoSelecionado;
	}

	public BigInteger getQtdSolicitada() {
		return qtdSolicitada;
	}

	public void setQtdSolicitada(BigInteger qtdSolicitada) {
		this.qtdSolicitada = qtdSolicitada;
	}

	public List<Pedido> getListaPedidoResult() {
		return listaPedidoResult;
	}

	public void setListaPedidoResult(List<Pedido> listaPedidoResult) {
		this.listaPedidoResult = listaPedidoResult;
	}

	public BigInteger getIdClienteRemover() {
		return idClienteRemover;
	}

	public void setIdClienteRemover(BigInteger idClienteRemover) {
		this.idClienteRemover = idClienteRemover;
	}

	public Date getDatPedidoSelecionada() {
		return datPedidoSelecionada;
	}

	public void setDatPedidoSelecionada(Date datPedidoSelecionada) {
		this.datPedidoSelecionada = datPedidoSelecionada;
	}

	public BigInteger getIdProdutoRemover() {
		return idProdutoRemover;
	}

	public void setIdProdutoRemover(BigInteger idProdutoRemover) {
		this.idProdutoRemover = idProdutoRemover;
	}

	public BigInteger getIdPedidoSelecionado() {
		return idPedidoSelecionado;
	}

	public void setIdPedidoSelecionado(BigInteger idPedidoSelecionado) {
		this.idPedidoSelecionado = idPedidoSelecionado;
	}

	public BigInteger getIdClienteSelecionado() {
		return idClienteSelecionado;
	}

	public void setIdClienteSelecionado(BigInteger idClienteSelecionado) {
		this.idClienteSelecionado = idClienteSelecionado;
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

	public String getDesClienteSelecionado() {
		return desClienteSelecionado;
	}

	public void setDesClienteSelecionado(String desClienteSelecionado) {
		this.desClienteSelecionado = desClienteSelecionado;
	}

	public List<OpcaoEntrega> getListaOpcaoEntrega() {
		return listaOpcaoEntrega;
	}

	public void setListaOpcaoEntrega(List<OpcaoEntrega> listaOpcaoEntrega) {
		this.listaOpcaoEntrega = listaOpcaoEntrega;
	}

	public PedidoDTO getPedidoDTO() {
		return pedidoDTO;
	}

	public void setPedidoDTO(PedidoDTO pedidoDTO) {
		this.pedidoDTO = pedidoDTO;
	}
}