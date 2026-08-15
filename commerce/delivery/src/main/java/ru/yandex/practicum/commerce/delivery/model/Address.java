package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.Embeddable;
import ru.yandex.practicum.commerce.api.dto.AddressDto;

@Embeddable
public class Address {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;

    public static Address from(AddressDto dto) {
        Address address = new Address();
        address.country = dto.country();
        address.city = dto.city();
        address.street = dto.street();
        address.house = dto.house();
        address.flat = dto.flat();
        return address;
    }

    public AddressDto toDto() { return new AddressDto(country, city, street, house, flat); }
}
