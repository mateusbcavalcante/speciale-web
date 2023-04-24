package br.com.a2dm.spdm.bean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import br.com.a2dm.brcmn.dto.PedidoDTO;
import br.com.a2dm.brcmn.dto.ProdutoDTO;
import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.brcmn.util.jsf.JSFUtil;
import br.com.a2dm.brcmn.util.jsf.Variaveis;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Cliente;
import br.com.a2dm.spdm.entity.Item;
import br.com.a2dm.spdm.entity.Pedido;
import br.com.a2dm.spdm.entity.Produto;
import br.com.a2dm.spdm.entity.SugestaoPedido;
import br.com.a2dm.spdm.omie.service.OmieProdutoEstruturaService;
import br.com.a2dm.spdm.omie.service.OmieProdutoService;
import br.com.a2dm.spdm.service.ClienteService;
import br.com.a2dm.spdm.service.ItemService;
import br.com.a2dm.spdm.service.PedidoService;
import br.com.a2dm.spdm.service.SugestaoPedidoService;

@RequestScoped
@ManagedBean
public class SugestaoPedidoBean extends AbstractBean<SugestaoPedido, SugestaoPedidoService>
{
	
	private JSFUtil util = new JSFUtil();
	private List<Produto> listaProduto;
	private Produto produto;
	private BigInteger qtdSolicitada;
	private Produto produtoSelecionado;
	private String informacao;
	private PedidoDTO pedidoDTO;
	private Pedido pedidoResult;
	private BigInteger idProdutoRemover;
	
	public SugestaoPedidoBean()
	{
		super(SugestaoPedidoService.getInstancia());

		this.ACTION_SEARCH = "sugestaoPedido";
		this.pageTitle = "Sugestão de Pedido";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
	}
	
	@Override
	public void preparaAlterar() {
		try
		{
			if (validarAcesso(Variaveis.ACAO_PREPARA_ALTERAR))
			{	
				Pedido pedido = new Pedido();
				pedido.setListaProduto(new ArrayList<>());
				
				Item item = new Item();
				item.setIdSugestaoPedido(this.getEntity().getIdSugestaoPedido());
				
				List<Item> itens = ItemService.getInstancia().pesquisar(item, 0);
				
				if (itens != null
						&& itens.size() > 0) {
					
					Optional<Item> ItemImagemOptional = this.getObjectImage(itens);
					this.getEntity().setImagem(ItemImagemOptional.isPresent() ? ItemImagemOptional.get().getUrl() : null);
					
					for (Item element: itens) {
						if (element.getIntegId() != null) {
							Produto produto = new Produto();
							produto.setIdProduto(element.getIntegId());
							produto.setDesProduto(element.getLabel());
							produto.setQtdSolicitada(element.getValue());
							produto.setFlgAtivo("S");
							produto.setUnidade(OmieProdutoEstruturaService.getInstance().
									obterProdutoEstrutura(produto.getIdProduto()).
									getIdentDTO().getUnidProduto()); 
							pedido.getListaProduto().add(produto);							
						}
					}
				}
				
				setPedidoResult(pedido);
				
				if (this.getEntity().getStatus().equalsIgnoreCase("Pendente")) {
					buscarProdutos();
				}

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
	
	public void buscarProdutos() throws Exception {
		this.setProdutoSelecionado(null);
		this.setInformacao(null);
		this.setProduto(new Produto());
		this.iniciaListaProdutos();
		
		Cliente cliente = new Cliente();
		cliente.setFlgAtivo("S");
		cliente.setIdExternoOmie(this.getEntity().getCodigoDestino());
		cliente = ClienteService.getInstancia().get(cliente, 0);
		
		if (cliente != null) {
			List<ProdutoDTO> listaProduto = OmieProdutoService.getInstance().listarProdutosPorTabelaPreco(cliente.getIdTabelaPrecoOmie());
			
			if (listaProduto != null && listaProduto.size() > 0) {
				for (ProdutoDTO element: listaProduto) { 
					Produto produto = new Produto();
					produto.setIdProduto(element.getIdProduto());
					produto.setDesProduto(element.getDesProduto());
					produto.setValorUnitario(element.getValorUnitario());
					produto.setQtdLoteMinimo(element.getQtdLoteMinimo());
					produto.setQtdMultiplo(element.getQtdMultiplo());
					this.getListaProduto().add(produto);
				}
			} else {
				this.setListaProduto(null);
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
	
	public void adicionaProdutoItens() {
		try {
			Item item = new Item();
			item.setIdSugestaoPedido(this.getEntity().getIdSugestaoPedido());
			item.setIntegId(produtoSelecionado.getIdProduto());
			item.setLabel(produtoSelecionado.getDesProduto());
			item.setValue(this.getQtdSolicitada());
			
			ItemService.getInstancia().inserir(item);
			
			// ADICIONANDO O PRODUTO
			Produto produto = new Produto();
			produto.setIdProduto(produtoSelecionado.getIdProduto());
			
			Optional<Produto> produtoOptional = this.getListaProduto().stream()
                    .filter(x -> x.getIdProduto() == this.getProduto().getIdProduto())
                    .findFirst();
			
			produto.setValorUnitario(produtoOptional.get().getValorUnitario());
			produto.setDesProduto(produtoOptional.get().getDesProduto());
			produto.setQtdSolicitada(this.getQtdSolicitada());
			produto.setFlgAtivo("S");
			produto.setUnidade(OmieProdutoEstruturaService.getInstance().
					obterProdutoEstrutura(produto.getIdProduto()).
					getIdentDTO().getUnidProduto()); 
			
			
			if (this.pedidoResult != null &&
					(this.pedidoResult.getListaProduto() == null || this.pedidoResult.getListaProduto().size() <= 0)) {
				this.pedidoResult.setListaProduto(new ArrayList<>());
			}
			
			this.pedidoResult.getListaProduto().add(produto);
			
			this.getProduto().setIdProduto(null);
			this.setQtdSolicitada(null);
			this.setProdutoSelecionado(null);
			
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	public void removerItem() 
	{
		try 
		{
			Item item = new Item();
			item.setIdSugestaoPedido(getEntity().getIdSugestaoPedido());
			item.setIntegId(this.getIdProdutoRemover());
			
			ItemService.getInstancia().removerItem(item);
			
			// REMOVENDO O PRODUTO
			Produto produto = new Produto();
			produto.setIdProduto(this.getIdProdutoRemover());
			
			OptionalInt indexOpt = IntStream.range(0, this.pedidoResult.getListaProduto().size())
				     .filter(i -> this.getIdProdutoRemover().equals(this.pedidoResult.getListaProduto().get(i).getIdProduto()))
				     .findFirst();
			
			this.pedidoResult.getListaProduto().remove(indexOpt.getAsInt());
		} 
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	public void atualizarQuantidade(Produto produto) 
	{
		try 
		{
			for (Produto element : pedidoResult.getListaProduto()) {
				if (element.getIdProduto() == produto.getIdProduto()) {
					element.setQtdSolicitada(produto.getQtdSolicitada());
					
					Item item = new Item();
					item.setIntegId(element.getIdProduto());
					item.setIdSugestaoPedido(this.getEntity().getIdSugestaoPedido());
					item.setValue(produto.getQtdSolicitada());
					
					ItemService.getInstancia().atualizarQuantidade(item);
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
	
	public void buscarInformacoes() throws Exception {
		this.setInformacao(null);
		if (this.getProduto().getIdProduto() != null && this.getProduto().getIdProduto().longValue() > 0) {
			Optional<Produto> produtoOptional = this.getListaProduto().stream()
					                                   .filter(x -> x.getIdProduto() == this.getProduto().getIdProduto())
					                                   .findFirst();
			
			if (produtoOptional.isPresent()) {
				produtoSelecionado = produtoOptional.get();
				
				if (produtoSelecionado.getValorUnitario() == null || produtoSelecionado.getValorUnitario() <= 0) {
					this.setInformacao("O produto selecionado não tem valor unitário.");
				}
			}
					                                
		} else {
			setProdutoSelecionado(null);
		}
	}
	
	@Override
	protected int getJoinPesquisar() {
		return SugestaoPedidoService.JOIN_PEDIDO_OPCAO_ENTREGA;
	}
	
	public void aprovar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				if (getEntity().getIdOpcaoEntrega() == null) {
					throw new Exception("O campo Opção de Entrega é obrigatório!");
				}
				
				if (existeItemSemValor()) {
					throw new Exception("Todos produtos devem possuir quantidade maior que 0!");
				}
				
				// CADASTRANDO PEDIDO NA OMIE
				Cliente cliente = new Cliente();
				cliente.setIdExternoOmie(getEntity().getCodigoDestino());
				cliente = ClienteService.getInstancia().get(cliente, 0);
				
				this.getPedidoResult().setCliente(cliente);
				this.getPedidoResult().setIdUsuarioCad(util.getUsuarioLogado().getIdUsuario());
				this.getPedidoResult().setDatCadastro(new Date());
				this.getPedidoResult().setFlgAtivo("S");
				this.getPedidoResult().setDatPedido(new Date());
				this.getPedidoResult().setIdOpcaoEntrega(getEntity().getIdOpcaoEntrega());
				this.getPedidoResult().setIdCodigoPedidoIntegracao(getEntity().getCodigoDestino());
				this.getPedidoResult().setObservacao("");
				
				PedidoService.getInstancia().inserirPedido(this.getPedidoResult());
				
				// INSERINDO ITENS
				this.getEntity().setItens(null);
				
				SugestaoPedidoService.getInstancia().aprovar(this.getEntity());
				
				FacesMessage message = new FacesMessage("A Sugestão de Pedido foi aprovada com sucesso!");
				message.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}		
	}
	
	public void reprovar() 
	{		
		try
		{
			if(this.getEntity() != null)
			{
				SugestaoPedidoService.getInstancia().reprovar(this.getEntity());
				FacesMessage message = new FacesMessage("A Sugestão de Pedido foi reprovada com sucesso!");
				message.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}		
	}
	
	private boolean existeItemSemValor() {
		for (Produto element : pedidoResult.getListaProduto()) {
			if (element.getQtdSolicitada() == null || element.getQtdSolicitada().equals(BigInteger.ZERO)) {
				return true;
			}
		}

		return false;
	}

	private List<Item> getItems(List<Item> itens) {
		return itens.stream().filter(x -> x.getValue() != null)
				             .collect(Collectors.toList());
	}

	private Optional<Item> getObjectImage(List<Item> itens) {
		return itens.stream().filter(x -> x.getLabel().equalsIgnoreCase(ItemService.OBJECT_CAPTURE_IMAGE))
							 .findFirst();
	}

	public List<Produto> getListaProduto() {
		return listaProduto;
	}

	public void setListaProduto(List<Produto> listaProduto) {
		this.listaProduto = listaProduto;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public BigInteger getQtdSolicitada() {
		return qtdSolicitada;
	}

	public void setQtdSolicitada(BigInteger qtdSolicitada) {
		this.qtdSolicitada = qtdSolicitada;
	}

	public Produto getProdutoSelecionado() {
		return produtoSelecionado;
	}

	public void setProdutoSelecionado(Produto produtoSelecionado) {
		this.produtoSelecionado = produtoSelecionado;
	}

	public String getInformacao() {
		return informacao;
	}

	public void setInformacao(String informacao) {
		this.informacao = informacao;
	}

	public PedidoDTO getPedidoDTO() {
		return pedidoDTO;
	}

	public void setPedidoDTO(PedidoDTO pedidoDTO) {
		this.pedidoDTO = pedidoDTO;
	}

	public Pedido getPedidoResult() {
		return pedidoResult;
	}

	public void setPedidoResult(Pedido pedidoResult) {
		this.pedidoResult = pedidoResult;
	}

	public BigInteger getIdProdutoRemover() {
		return idProdutoRemover;
	}

	public void setIdProdutoRemover(BigInteger idProdutoRemover) {
		this.idProdutoRemover = idProdutoRemover;
	}
}