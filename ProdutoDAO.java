package com.estoque;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO implements AutoCloseable{
    private Connection conexao;

    public ProdutoDAO(String caminhoBanco){
        try{
            Class.forName("org.sqlite.JDBC");
            this.conexao = DriverManager.getConnection("jdbc:sqlite:" + caminhoBanco);
            criarTabela();
        } catch(ClassNotFoundException | SQLException e){
            throw new RuntimeException("Erro ao conectar ao banco de dados SQLite: " + e.getMessage());
        }
    }

    private void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS produtos (" +
                "id INTERGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL," +
                "sku TEXT NOT NULL UNIQUE," +
                "preco REAL NOT NULL CHECK (preco > 0)," +
                "quantidadeEstoque INTEGER NOT NULL CHECK (quantidadeEstoque > 0)," +
                "ativo INTERGER DEFAULT 1" +
                ");";
        try(Statement stmt = conexao.createStatement()){
            stmt.execute(sql);
        } catch(SQLException e){
            throw new RuntimeException("Erro ao criar tabela de produtos: " + e.getMessage());
        }
    }

    public boolean salvar(Produto produto){
        String sql = "INSERT INTO produtos (nome, sku, preco, quantidadeEstoque, ativo) VALUES (?, ?, ?, ?, ?)";
        try(PreparedStatement pstmt = conexao.prepareStatement(sql)){
            pstmt.setString(1, produto.getNome());
            pstmt.setString(2, produto.getSku());
            pstmt.setDouble(3, produto.getPreco());
            pstmt.setInt(4, produto.getQuantidadeEstoque());
            pstmt.setInt(5, produto.isAtivo() ? 1 : 0);
            pstmt.executeUpdate();
            return true;
        } catch(SQLException e){

            if(e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")){
                System.out.println("Erro: O SKU '" + produto.getSku() + "' já existe no banco de dados.");
            } else {
                System.out.println("Erro ao salvar produto: " + e.getMessage());
            }
            return false;
        }
    }

    public Produto buscarporId(int id){
        String sql = "SELECT * FROM produtos WHERE id = ?";
        try(PreparedStatement pstmt = conexao.prepareStatement(sql)){
            pstmt.setInt(1, id);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return extrairProdutoDoResultSet(rs);
                }
            }
        } catch(SQLException e){
            System.out.println("[Erro] erro ao buscar produto por ID: " + e.getMessage());
        }
        return null;
    }

    public Produto buscarporSku(String sku){
        String sql = "SELECT * FROM produtos WHERE sku = ?";
        try(PreparedStatement pstmt = conexao.prepareStatement(sql)){
            pstmt.setString(1, sku);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return extrairProdutoDoResultSet(rs);
                }
            }
        } catch(SQLException e){
            System.out.println("[Erro] erro ao buscar produto por SKU: " + e.getMessage());
        }
        return null;
    }

    public List<Produto> listarAtivos(){
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos WHERE ativo = 1";
        try(Statement stmt = conexao.createStatement()){
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                produtos.add(extrairProdutoDoResultSet(rs));
            }
        } catch(SQLException e){
            System.out.println("[Erro] erro ao listar produtos ativos: " + e.getMessage());
        }
        return produtos;
    }

    public boolean atualizarEstoque(int id, int QuantidadeNova){
        if(QuantidadeNova < 0){
            throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa.");
        }

        String sql = "UPDATE produtos SET quantidadeEstoque = ? WHERE id = ?";
        try(PreparedStatement psmt = conexao.prepareStatement(sql)){
            psmt.setInt(1, QuantidadeNova);
            psmt.setInt(2, id);
            int linhasAfetadas = psmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch(SQLException e){
            System.out.println("[Erro] erro ao atualizar estoque: " + e.getMessage());
            return false;
        }
    }

    public boolean DesativarProduto(int id){
        String sql = "UPDATE produtos SET ativo = 0 WHERE id = ?";
        try(PreparedStatement psmt = conexao.prepareStatement(sql)){
            psmt.setInt(1, id);
            int linhasAfetadas = psmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch(SQLException e){
            System.out.println("[Erro] erro ao desativar produto: " + e.getMessage());
            return false;
        }
    }

    private Produto extrairProdutoDoResultSet(ResultSet rs) throws SQLException{
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String sku = rs.getString("sku");
        double preco = rs.getDouble("preco");
        int qtd = rs.getInt("quantidadeEstoque");
        boolean ativo = rs.getInt("ativo") == 1;
        return new Produto(id, nome, sku, preco, qtd, ativo);
    }

    @Override 
    public void close(){
        try{
            if(conexao != null && !conexao.isClosed()){
                conexao.close();
            }
        } catch(SQLException e){
            System.out.println("[Erro] erro ao fechar conexão com o banco de dados: " + e.getMessage());
        }
    }

    public void desativarPorSku(String sku) {
        String sql = "UPDATE produtos SET ativo = 0 WHERE sku = ?";
        try (PreparedStatement psmt = conexao.prepareStatement(sql)) {
            psmt.setString(1, sku);
            int linhasAfetadas = psmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Produto com SKU '" + sku + "' desativado com sucesso.");
            } else {
                System.out.println("Nenhum produto encontrado com SKU '" + sku + "'.");
            }
        } catch (SQLException e) {
            System.out.println("[Erro] erro ao desativar produto por SKU: " + e.getMessage());
        }
    }
}
