package com.proj.weather_consumer.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.proj.weather_consumer.model.User;
import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    //Custom method to find users who match a city and a notification preference
    List<User> findByCityAndNotificationPreference(String city, String notificationPreference);
}
