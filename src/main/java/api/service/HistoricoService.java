package api.service;

import api.DAO.historicoDAO;

public class HistoricoService {

    public static void registrar(String entidade,
                                 String acao,
                                 Integer entidadeId,
                                 String descricao) {

        historicoDAO.registrar(entidade, acao, entidadeId, descricao);
    }
}