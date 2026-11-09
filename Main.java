package com.estoque;

import java.io.File;

public class Main {
     
    public static void main(String[] args) {
        File dir = new File("db");
        if (!dir.exists()) {
            dir.mkdir();
        }

        String caminhoBanco = "db/estoque.db";

    System.out.println("\n SISTEMA DE GERENCIAMENTO DE ESTOQUE (E-COMMERCE)");
    
    try(ProdutoDAO dao = new ProdutoDAO(caminhoBanco)){
            System.out.println("\n--- 1. CADASTRAR PRODUTO ---");
            dao.salvar(new Produto("teclado mecânico", "TEC-MEC-001", 299.90, 15));
            dao.salvar(new Produto("mouse gamer", "MOU-GAM-002", 150.50, 30));
            dao.salvar(new Produto("monitor 4K", "MON-4K-003", 1899.00, 5));

            System.out.println("\n--- TESTANDO SKU DUPLICADO ---");
            dao.salvar(new Produto("outro teclado", "TEC-MEC-001", 299.90, 10));

            System.out.println("\n--- 2. LISTAR PRODUTOS ATIVOS ---");
            dao.listarAtivos().forEach(System.out::println);

            System.out.println("\n--- 3. ATUALIZANDO ESTOQUE (ID 1) ---");
            dao.atualizarEstoque(1, 12);
            
            try {
                System.out.println("testando definir estoque negativo...");
                dao.atualizarEstoque(1, -2);
            } catch (IllegalArgumentException e) {
                System.out.println("exceção capturada com sucesso: " + e.getMessage());
            }

            System.out.println("\n--- 4. BUSCAR PRODUTO ATUALIZADO POR SKU ---");
            Produto pAtualizado = dao.buscarporSku("TEC-MEC-001");
            if (pAtualizado != null) {
                System.out.println(pAtualizado);
            }

            System.out.println("\n--- 5. DESATIVANDO O MONITOR 4K (Soft delete) ---");
            
            dao.desativarPorSku("MON-4K-003"); 
            
            System.out.println("\n--- 6. LISTANDO ATIVOS APÓS DESATIVAÇÃO ---");
            dao.listarAtivos().forEach(System.out::println);

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
         System.out.println("\n--- FIM DA EXECUÇÃO ---");
    }
}