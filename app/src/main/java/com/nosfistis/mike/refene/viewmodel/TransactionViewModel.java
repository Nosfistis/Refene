package com.nosfistis.mike.refene.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.nosfistis.mike.refene.TransactionRepository;
import com.nosfistis.mike.refene.database.Transaction;

import java.util.List;

public class TransactionViewModel extends AndroidViewModel {

    private TransactionRepository repository;
    private LiveData<List<Transaction>> transactions;

    public TransactionViewModel(Application application) {
        super(application);

        repository = new TransactionRepository(application);
    }
}
