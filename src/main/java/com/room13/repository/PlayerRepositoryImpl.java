package com.room13.repository;

import com.room13.model.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PlayerRepositoryImpl implements PlayerRepository {

    @Autowired
    private SessionFactory sessionFactory;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Player save(Player player) {
        currentSession().saveOrUpdate(player);
        return player;
    }

    @Override
    @Transactional(readOnly = true)
    public Player findByUsername(String username) {
        Query<Player> query = currentSession()
                .createQuery("FROM Player WHERE username = :username", Player.class);
        query.setParameter("username", username);
        return query.uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Player findById(Long id) {
        return currentSession().get(Player.class, id);
    }

    @Override
    public void updateBestScore(Long playerId, int score) {
        Query<?> query = currentSession()
                .createQuery("UPDATE Player SET bestScore = :score WHERE id = :id AND bestScore < :score");
        query.setParameter("score", score);
        query.setParameter("id", playerId);
        query.executeUpdate();
    }
}
