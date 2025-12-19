package com.fhm.take2;

import com.Client;
import com.crdt.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;

public class AnalyticsController {

    @FXML private Label postCountLabel;
    @FXML private Label subCountLabel;
    @FXML private Label userCountLabel;

    @FXML
    public void Init(User user) {
        try {
            ArrayList<Integer> arr = Client.GetAnalytics(user);
            postCountLabel.setText(String.valueOf(arr.get(0)));
            subCountLabel.setText(String.valueOf(arr.get(1)));
            userCountLabel.setText(String.valueOf(arr.get(2)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}