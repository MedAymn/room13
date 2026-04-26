package com.room13.repository;

import com.room13.model.SavedGame;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class SavedGameRepositoryImpl implements SavedGameRepository {

    @Autowired
    private SessionFactory sessionFactory;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public SavedGame save(SavedGame savedGame) {
        currentSession().save(savedGame);
        return savedGame;
    }

    @Override
    public SavedGame update(SavedGame savedGame) {
        currentSession().update(savedGame);
        return savedGame;
    }

    @Override
    @Transactional(readOnly = true)
    public SavedGame findById(Long id) {
        return currentSession().get(SavedGame.class, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedGame> findByPlayerId(Long playerId) {
        Query<SavedGame> query = currentSession()
                .createQuery("FROM SavedGame sg WHERE sg.player.id = :playerId ORDER BY sg.savedAt DESC", SavedGame.class);
        query.setParameter("playerId", playerId);
        return query.getResultList();
    }

    @Override
    public void deleteById(Long id) {
        SavedGame sg = currentSession().get(SavedGame.class, id);
        if (sg != null) {
            currentSession().delete(sg);
        }
    }
}
