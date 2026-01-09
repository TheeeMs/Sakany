package com.theMs.sakany.shared.cqrs;

public interface CommandHandler<C extends Command<R>,R>{
    R handle(C command);
}
