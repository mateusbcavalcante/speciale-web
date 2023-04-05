package br.com.a2dm.spdm.bean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import br.com.a2dm.brcmn.dto.ProdutoDTO;
import br.com.a2dm.brcmn.util.jsf.AbstractBean;
import br.com.a2dm.spdm.config.MenuControl;
import br.com.a2dm.spdm.entity.Cliente;
import br.com.a2dm.spdm.entity.Item;
import br.com.a2dm.spdm.entity.Produto;
import br.com.a2dm.spdm.entity.SugestaoPedido;
import br.com.a2dm.spdm.omie.service.OmieProdutoService;
import br.com.a2dm.spdm.service.ClienteService;
import br.com.a2dm.spdm.service.ItemService;
import br.com.a2dm.spdm.service.SugestaoPedidoService;

@RequestScoped
@ManagedBean
public class SugestaoPedidoBean extends AbstractBean<SugestaoPedido, SugestaoPedidoService>
{
	
	private List<Produto> listaProduto;
	private BigInteger idProdutoAdicionar;
	private BigInteger qtdSolicitada;
	
	public SugestaoPedidoBean()
	{
		super(SugestaoPedidoService.getInstancia());
		this.ACTION_SEARCH = "sugestaoPedido";
		this.pageTitle = "Sugestão de Pedido";
		
		MenuControl.ativarMenu("flgMenuMan");
		MenuControl.ativarSubMenu("flgMenuManCli");
	}
	
	public void visualizar()
	{
		try
		{
			if (this.getEntity().getStatus().equalsIgnoreCase("Pendente")) {
				buscarProdutos();
			}
			
			Item item = new Item();
			item.setIdSugestaoPedido(this.getEntity().getIdSugestaoPedido());
			
			List<Item> itens = ItemService.getInstancia().pesquisar(item, 0);
			
			if (itens != null && itens.size() > 0) {
				Optional<Item> ItemImagemOptional = this.getObjectImage(itens);
				itens = this.getItems(itens);
				this.getEntity().setImagem(ItemImagemOptional.isPresent() ? ItemImagemOptional.get().getUrl() : null);
				this.getEntity().setItens(itens);
			} else {
				setEntity(new SugestaoPedido());
			}
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	public void buscarProdutos() throws Exception {
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
			Item itemInserido = ItemService.getInstancia().inserir(item);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removerItem(Item item) 
	{		
		try 
		{
			ItemService.getInstancia().removerItem(item);
			
			Item filtroItem = new Item();
			filtroItem.setIdSugestaoPedido(this.getEntity().getIdSugestaoPedido());
			
			List<Item> novosItens = ItemService.getInstancia().pesquisar(filtroItem, 0);
			this.getEntity().setItens(novosItens);
		} 
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	public void atualizarQuantidade(Item item) 
	{
		try 
		{
			ItemService.getInstancia().atualizarQuantidade(item);
		}
		catch (Exception e) 
		{
			FacesMessage message = new FacesMessage(e.getMessage());
	        message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        FacesContext.getCurrentInstance().addMessage(null, message);
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
				this.getEntity().setItens(null);
				SugestaoPedidoService.getInstancia().aprovar(this.getEntity());
				this.getEntity().setOpcaoEntrega(this.getEntity().getOpcaoEntrega());
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

	public BigInteger getIdProdutoAdicionar() {
		return idProdutoAdicionar;
	}

	public void setIdProdutoAdicionar(BigInteger idProdutoAdicionar) {
		this.idProdutoAdicionar = idProdutoAdicionar;
	}

	public BigInteger getQtdSolicitada() {
		return qtdSolicitada;
	}

	public void setQtdSolicitada(BigInteger qtdSolicitada) {
		this.qtdSolicitada = qtdSolicitada;
	}
	
}