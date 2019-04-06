package com.nosfistis.mike.refene.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.nosfistis.mike.refene.RefeneRepository;
import com.nosfistis.mike.refene.database.Refene;

import java.util.List;
import java.util.Objects;

public class RefeneViewModel extends AndroidViewModel {

    private RefeneRepository repository;
    private LiveData<List<Refene>> refenes;

    public RefeneViewModel(Application application) {
        super(application);

        repository = new RefeneRepository(application);
        refenes = repository.getAllRefenes();
    }

    public LiveData<List<Refene>> getAllRefenes() {
        return refenes;
    }

    public Refene getRefeneById(long refId) {
        for (Refene refene : Objects.requireNonNull(refenes.getValue())) {
            if (refene.getId() == refId) {
                return refene;
            }
        }

        return null;
    }

    public void insert(Refene refene) {
        repository.insert(refene);
    }

    public LiveData<Long> getLastInsertId() {
        return repository.getLastInsertId();
    }
}
